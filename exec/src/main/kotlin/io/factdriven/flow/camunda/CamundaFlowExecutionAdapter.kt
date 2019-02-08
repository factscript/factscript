package io.factdriven.flow.camunda

import io.factdriven.flow.Flows
import io.factdriven.flow.lang.ActionClassifier
import io.factdriven.flow.lang.MessageReaction
import io.factdriven.flow.lang.*
import io.factdriven.flow.view.transform
import io.factdriven.flow.view.translate
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
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration
import org.camunda.spin.impl.json.jackson.JacksonJsonNode
import org.camunda.spin.plugin.variable.SpinValues
import org.camunda.spin.plugin.variable.value.JsonValue
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity
import org.slf4j.Logger
import sun.plugin2.util.PojoUtil.toJson


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
const val MESSAGE_NAME_VAR = "message"
const val MESSAGES_VAR = "messages"

val log: Logger = LoggerFactory.getLogger(CamundaBpmFlowExecutor::class.java)

class CamundaFlowTransitionListener: ExecutionListener {

    private lateinit var target: Expression

    override fun notify(execution: DelegateExecution) {

        val messages = Messages.fromJson(execution.getVariableTyped<JsonValue>(MESSAGES_VAR, false).valueSerialized!!).toMutableList()

        fun pattern(node: Node): MessagePattern? {
            return when (node) {
                is MessageReaction -> node.match(node.root.type.apply(messages)).firstOrNull()
                is Flow<*> -> node.getNode(ActionClassifier.Success)?.let { pattern(it) }
                else -> null
            }
        }

        pattern(Flow.getNode(target.getValue(execution).toString()))?.hash?.let { execution.setVariable(MESSAGE_NAME_VAR, it) }

    }

}

class CamundaFlowNodeStartListener: ExecutionListener {

    override fun notify(execution: DelegateExecution) {

        val node = Flow.getNode(execution.currentActivityId)
        val messages = Messages.fromJson(execution.getVariableTyped<JsonValue>(MESSAGES_VAR, false).valueSerialized!!).toMutableList()
        fun aggregate() = node.root.type.apply(messages)
        fun command() = (execution.processEngine as ProcessEngineImpl).processEngineConfiguration.commandExecutorTxRequired

        fun message(node: Node): Message<*>? {
            return when(node) {
                is Action -> node.function?.invoke(aggregate())?. let { Message(it) }
                is MessageReaction -> node.action?.function?.invoke(aggregate(), messages.last().fact)?.let { Message(it) }
                is Flow<*> -> node.getNode(ActionClassifier.Intention)?.let { message(it) }
                else -> throw IllegalArgumentException()
            }
        }

        message(node)?.let {
            messages.add(it)
            execution.setVariable(MESSAGES_VAR, SpinValues.jsonValue(messages.toJson()))
            command().execute(CreateCamundaBpmFlowJob(it))
            log.debug("Outgoing: ${it.fact}")
            log.debug("> Status: ${aggregate()}")
        }

    }

}

object CamundaBpmFlowExecutor {

    private val engine: ProcessEngine = ProcessEngines.getProcessEngines().values.first()

    fun <F: Fact> target(message: Message<F>) : List<Message<F>> {

        log.debug("Incoming: ${message.fact}")

        val targets = Flows.match(message).map { pattern ->

            listOf(
                engine.externalTaskService.fetchAndLock(Int.MAX_VALUE, pattern.hash).topic(pattern.hash, Long.MAX_VALUE).execute().map { task ->
                    message.target(MessageTarget(pattern.entityType.simpleName!!, task.businessKey, pattern.hash))
                },
                engine.runtimeService.createEventSubscriptionQuery().eventType(EventType.MESSAGE.name()).eventName(pattern.hash).list().map { subscription ->
                    val businessKey = subscription.processInstanceId?.let { engine.runtimeService.createProcessInstanceQuery().processInstanceId(subscription.processInstanceId).singleResult()?.businessKey }
                    message.target(MessageTarget(pattern.entityType.simpleName!!, businessKey, pattern.hash))
                }
            ).flatten()

        }.flatten()

        if (targets.isEmpty()) {
            log.debug("> Target: None")
        }
        return targets

    }

    fun correlate(message: Message<*>) {

        assert(message.target != null) { "Correlation only works for messages with a specified match!" }

        val flowName = message.target!!.first
        val flowDefinition = Flows.get(flowName)
        val businessKey = message.target!!.second
        val correlationHash = message.target!!.third
        val processInstanceId = businessKey?.let { engine.runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult()?.id }

        fun variables(): Map<String, Any> {

            val messages = processInstanceId?.let { pid ->
                engine.runtimeService.getVariableTyped<JsonValue>(pid, MESSAGES_VAR, false)?.valueSerialized?.let { Messages.fromJson(it) }
            } ?: emptyList()

            with(messages.toMutableList()) {
                add(message)
                log.debug("> Target: ${flowDefinition.type.apply(this)}")
                return mapOf(MESSAGES_VAR to SpinValues.jsonValue(toJson()).create())
            }

        }

        if (processInstanceId != null) {

            val externalTask = engine.externalTaskService
                .createExternalTaskQuery()
                .processInstanceId(processInstanceId)
                .topicName(correlationHash)
                .singleResult()

            if (externalTask != null) {
                engine.externalTaskService.complete(
                    externalTask.id,
                    correlationHash,
                    variables()
                )
                return
            }

        }

        val correlationBuilder = engine.runtimeService
            .createMessageCorrelation(correlationHash)
            .setVariables(variables())
        if (processInstanceId != null) {
            correlationBuilder.processInstanceId(processInstanceId)
        } else {
            correlationBuilder.processInstanceBusinessKey(message.id)
        }
        correlationBuilder.correlate()

    }

    fun <ENTITY: Entity> load(id: EntityId, type: KClass<ENTITY>): ENTITY {

        val processInstance =
            engine.historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(id)
                .singleResult()

        val messages = engine.historyService.createHistoricVariableInstanceQuery()
            .processInstanceId(processInstance.id)
            .variableName(MESSAGES_VAR)
            .disableCustomObjectDeserialization()
            .singleResult().value as JacksonJsonNode?

        return messages?.let { type.apply(Messages.fromJson(messages.unwrap())) } ?: throw IllegalArgumentException()

    }

}

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

        val message = Messages.fromJson(configuration!!.message)[0]
        if (message.target == null) {
            CamundaBpmFlowExecutor.target(message).forEach {
                (commandContext!!.processEngineConfiguration.commandExecutorTxRequired.execute(CreateCamundaBpmFlowJob(it)))
            }
        } else {
            CamundaBpmFlowExecutor.correlate(message)
        }

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

class CreateCamundaBpmFlowJob(private val message: Message<*>) : Command<String> {

    override fun execute(commandContext: CommandContext): String {

        val job = MessageEntity()
        job.jobHandlerType = CamundaBpmFlowJobHandler.TYPE
        job.jobHandlerConfiguration = CamundaBpmFlowJobHandlerConfiguration(message.toJson())
        Context.getCommandContext().jobManager.send(job)
        return job.id

    }

}

class CamundaFlowExecutionPlugin: ProcessEnginePlugin {

    override fun preInit(configuration: ProcessEngineConfigurationImpl) {
        configuration.customJobHandlers = configuration.customJobHandlers ?: emptyList()
        configuration.customJobHandlers.add(CamundaBpmFlowJobHandler())
    }

    override fun postInit(configuration: ProcessEngineConfigurationImpl) {
        //
    }

    override fun postProcessEngineBuild(engine: ProcessEngine) {
        Flows.all.forEach { flowDefinition ->
            val bpmn = transform(translate(flowDefinition))
            engine.repositoryService
                .createDeployment()
                .addModelInstance("${flowDefinition.name}.bpmn", bpmn)
                .name(flowDefinition.name)
                .deploy()
        }
    }

}