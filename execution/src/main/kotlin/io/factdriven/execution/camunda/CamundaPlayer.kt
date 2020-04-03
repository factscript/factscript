package io.factdriven.execution.camunda

import io.factdriven.definition.*
import io.factdriven.definition.api.Consuming
import io.factdriven.definition.api.Calling
import io.factdriven.definition.api.Node
import io.factdriven.definition.api.Throwing
import io.factdriven.execution.*
import io.factdriven.visualization.bpmn.*
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngines
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.delegate.Expression
import org.camunda.bpm.engine.impl.ProcessEngineImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.event.EventType
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity
import org.camunda.spin.impl.json.jackson.JacksonJsonNode
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin
import org.camunda.spin.plugin.variable.SpinValues
import org.camunda.spin.plugin.variable.value.JsonValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val log: Logger = LoggerFactory.getLogger(Player::class.java)

class CamundaRepository: Repository {

    private val engine: ProcessEngine get() = ProcessEngines.getProcessEngines().values.first()

    override fun load(id: String): List<Message> {
        val processInstance =
            engine.historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(id)
                .singleResult()
        val messages = engine.historyService.createHistoricVariableInstanceQuery()
            .processInstanceId(processInstance.id)
            .variableName(MESSAGES_VAR)
            .disableCustomObjectDeserialization()
            .singleResult().value as JacksonJsonNode?
        return messages?.let { Message.list.fromJson(messages.unwrap()) } ?: throw IllegalArgumentException()
    }

}

class CamundaProcessor: Processor {

    private val engine: ProcessEngine get() = ProcessEngines.getProcessEngines().values.first()

    override fun process(message: Message) {
        if (message.handler != null) {
            handle(message)
        } else {
            route(message)
        }
    }

    private fun route(message: Message) {

        val messages = Flows.handling(message).map { handling ->

            fun messagesHandledByExternalTasks() : List<Message> {
                val externalTasksHandlingMessage =  engine.externalTaskService
                    .fetchAndLock(Int.MAX_VALUE, handling.hash)
                    .topic(handling.hash, Long.MAX_VALUE)
                    .execute()
                return externalTasksHandlingMessage.map { task ->
                    Message.handle(message, Handler(StreamId(task.processDefinitionKey, task.businessKey), handling))
                }
            }

            fun messagesHandledByEventSubscriptions() : List<Message> {

                val eventSubscriptionsHandlingMessage = engine.runtimeService.createEventSubscriptionQuery()
                    .eventType(EventType.MESSAGE.name())
                    .eventName(handling.hash)
                    .list()

                val businessKeysOfRunningProcessInstances = eventSubscriptionsHandlingMessage.map { subscription ->
                    subscription.processInstanceId?.let {
                        engine.runtimeService.createProcessInstanceQuery()
                            .processInstanceId(subscription.processInstanceId)
                            .singleResult().businessKey
                    }
                }

                return eventSubscriptionsHandlingMessage.mapIndexed { index, subscription ->
                    val processDefinitionKey = subscription.activityId.substring(0, subscription.activityId.indexOf("-"))
                    Message.handle(message, Handler(StreamId(processDefinitionKey, businessKeysOfRunningProcessInstances[index]), handling))
                }

            }

            listOf(messagesHandledByExternalTasks(), messagesHandledByEventSubscriptions()).flatten()

        }.flatten()

        messages.forEach {
            Player.publish(it)
        }

    }

    private fun handle(message: Message) {

        val handler = message.handler?.stream
            ?: throw IllegalArgumentException("Messages not (yet) routed to receiver!")

        val processInstanceId = handler.id?.let {
            engine.runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(handler.id)
                .singleResult()
                ?.id
        }

        fun variables(): Map<String, Any> {

            val messages = Player.load(handler.id)

            with(messages.toMutableList()) {
                add(message)
                return mapOf(
                    MESSAGES_VAR to SpinValues.jsonValue(toJson()).create()
                )
            }

        }

        if (processInstanceId != null) {

            val externalTask = engine.externalTaskService
                .createExternalTaskQuery()
                .processInstanceId(processInstanceId)
                .topicName(message.handler!!.handling.hash)
                .singleResult()

            if (externalTask != null) {
                engine.externalTaskService.complete(
                    externalTask.id,
                    message.handler!!.handling.hash,
                    variables()
                )
                return
            }

        }

        val correlationBuilder = engine.runtimeService
            .createMessageCorrelation(message.handler!!.handling.hash)
            .setVariables(variables())
        if (processInstanceId != null) {
            correlationBuilder.processInstanceId(processInstanceId)
        } else {
            correlationBuilder.processInstanceBusinessKey(message.fact.id)
        }
        correlationBuilder.correlate()

    }

}

class CamundaPublisher: Publisher {

    private val engine: ProcessEngineImpl get() = ProcessEngines.getProcessEngines().values.first() as ProcessEngineImpl

    override fun publish(vararg messages: Message) {
        messages.forEach { message ->
            engine.processEngineConfiguration.commandExecutorTxRequired.execute(CreateCamundaBpmFlowJob(message))
        }
    }

    class CreateCamundaBpmFlowJob(private val message: Message) : Command<String> {

        override fun execute(commandContext: CommandContext): String {

            val job = MessageEntity()
            job.jobHandlerType = CamundaBpmFlowJobHandler.TYPE
            job.jobHandlerConfiguration = CamundaBpmFlowJobHandlerConfiguration(message.toJson())
            Context.getCommandContext().jobManager.send(job)
            return job.id

        }

    }

}


class CamundaFlowTransitionListener: ExecutionListener {

    private lateinit var target: Expression

    override fun notify(execution: DelegateExecution) {

        val nodeId = target.getValue(execution).toString()
        val handling = when (val node = Flows.getNodeById(nodeId)) {
            is Consuming -> node.endpoint(execution)
            is Calling -> node.endpoint(execution)
            else -> null
        }
        execution.setVariable(MESSAGE_NAME_VAR, handling?.hash)

    }

    private fun Consuming.endpoint(execution: DelegateExecution): Handling {
        val messageString = execution.getVariableTyped<JsonValue>(MESSAGES_VAR, false).valueSerialized
        val messages = Message.list.fromJson(messageString)
        val handlerInstance = Player.load(messages, entity)
        val details = properties.mapIndexed { propertyIndex, propertyName ->
            propertyName to matching[propertyIndex].invoke(handlerInstance)
        }.toMap()
        return Handling(catching, details)
    }

    private fun Calling.endpoint(execution: DelegateExecution): Handling {
        val messageString = execution.getVariableTyped<JsonValue>(MESSAGES_VAR, false).valueSerialized
        val messages = Message.list.fromJson(messageString)
        return Handling(catching, MessageId.nextId(messages.last().id))
    }

}

class CamundaFlowNodeStartListener: ExecutionListener {

    override fun notify(execution: DelegateExecution) {

        val definition = Flows.getDefinitionById(execution.currentActivityId)
        val node = Flows.getNodeById(execution.currentActivityId)
        val messages = Message.list.fromJson(execution.getVariableTyped<JsonValue>(MESSAGES_VAR, false).valueSerialized!!).toMutableList()
        fun aggregate() = messages.applyTo(node.entity)

        fun message(node: Node): Message? {
            return when(node) {
                is Throwing -> {
                    val fact = node.instance.invoke(aggregate())
                    val correlating = definition.getPromising().succeeding?.isInstance(fact) ?: false
                    Message.from(messages, Fact(fact), if (correlating) messages.first().id else null)
                }
                else -> null
            }
        }

        message(node)?.let {
            messages.add(it)
            execution.setVariable(MESSAGES_VAR, SpinValues.jsonValue(messages.toJson()))
            Player.publish(it)
        }

    }

}

private const val MESSAGES_VAR = "messages"
private const val MESSAGE_NAME_VAR = "message"

class CamundaBpmFlowJobHandler: JobHandler<CamundaBpmFlowJobHandlerConfiguration> {

    override fun getType(): String {
        return TYPE
    }

    override fun execute(
        configuration: CamundaBpmFlowJobHandlerConfiguration?,
        execution: ExecutionEntity?,
        commandContext: CommandContext?,
        tenantId: String?
    ) {
        Player.process(Message.fromJson(configuration!!.message))
    }

    override fun newConfiguration(canonicalString: String?): CamundaBpmFlowJobHandlerConfiguration {
        return CamundaBpmFlowJobHandlerConfiguration(canonicalString!!)
    }

    override fun onDelete(configuration: CamundaBpmFlowJobHandlerConfiguration?, jobEntity: JobEntity?) {
        //
    }

    companion object {

        const val TYPE = "flowJobHandler"

    }

}

data class CamundaBpmFlowJobHandlerConfiguration(var message: String = ""): JobHandlerConfiguration {

    override fun toCanonicalString(): String? {
        return message
    }

}

class CamundaFlowExecutionPlugin: ProcessEnginePlugin {

    override fun preInit(configuration: ProcessEngineConfigurationImpl) {
        configuration.customJobHandlers = configuration.customJobHandlers ?: mutableListOf()
        configuration.processEnginePlugins = configuration.processEnginePlugins + SpinProcessEnginePlugin()
        configuration.customJobHandlers.add(CamundaBpmFlowJobHandler())
    }

    override fun postInit(configuration: ProcessEngineConfigurationImpl) {
        //
    }

    override fun postProcessEngineBuild(engine: ProcessEngine) {

        Flows.all.values.forEach { definition ->
            val bpmn = transform(translate(definition))
            engine.repositoryService
                .createDeployment()
                .addModelInstance("${definition.id}.bpmn", bpmn)
                .name(definition.id)
                .deploy()
        }

    }

}