package io.factdriven.flow.camunda

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


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
const val MESSAGE_NAME_VAR = "message"
const val MESSAGES_VAR = "messages"

val log = LoggerFactory.getLogger(CamundaBpmFlowExecutor::class.java)

class CamundaFlowTransitionListener: ExecutionListener {

    private lateinit var target: Expression

    override fun notify(execution: DelegateExecution) {

        val element = FlowDefinitions.getElementById(target.getValue(execution).toString())
        val messages = element.root.deserialize(execution.getVariableTyped<JsonValue>(MESSAGES_VAR, false).valueSerialized!!).toMutableList()

        fun aggregate() = element.root.aggregate(messages.map { it.fact })

        fun pattern(element: FlowElement): MessagePattern? {
            return when (element) {
                is FlowMessageReactionDefinition -> {
                    element.expected(aggregate())
                }
                is FlowDefinition<*> -> {
                    val success = element.getChildByActionType(FlowActionType.Success)
                    if (success != null) pattern(success) else null
                }
                else -> null
            }
        }

        val pattern = pattern(element)

        execution.setVariable(MESSAGE_NAME_VAR, pattern?.hash)

    }

}

class CamundaFlowNodeStartListener: ExecutionListener {

    override fun notify(execution: DelegateExecution) {

        val element = FlowDefinitions.getElementById(execution.currentActivityId)
        val messages = element.root.deserialize(execution.getVariableTyped<JsonValue>(MESSAGES_VAR, false).valueSerialized!!).toMutableList()

        fun aggregate() = element.root.aggregate(messages.map { it.fact })

        fun message(element: FlowElement): Message<*>? {
            return when(element) {
                is FlowActionDefinition -> {
                    val action = element.function
                    val fact = action?.invoke(aggregate())
                    if (fact != null) Message.from(fact) else null
                }
                is FlowMessageReactionDefinition -> {
                    val action = element.action?.function
                    val fact = action?.invoke(aggregate(), messages.last().fact)
                    if (fact != null) Message.from(fact) else null
                }
                is FlowDefinition<*> -> {
                    val intent = element.getChildByActionType(FlowActionType.Intent)
                    if (intent != null) message(intent) else null
                }
                else -> throw IllegalArgumentException()
            }
        }

        val message = message(element)
        if (message != null) {
            log.debug("Outgoing: ${message.fact}")
            messages.add(message)
            (execution.processEngine as ProcessEngineImpl).processEngineConfiguration.commandExecutorTxRequired.execute(
                CreateCamundaBpmFlowJob(message)
            )
            log.debug("> Status: ${aggregate()}")
        }

        execution.setVariable(MESSAGES_VAR, SpinValues.jsonValue(element.root.serialize(messages)))

    }

}

object CamundaBpmFlowExecutor {

    val engine: ProcessEngine = ProcessEngines.getProcessEngines().values.first()

    fun <F: Fact> target(message: Message<F>) : List<Message<F>> {

        log.debug("Incoming: ${message.fact}")

        val targets = FlowDefinitions.all().map { definition ->

            definition.patterns(message.fact).map { pattern ->

                listOf(
                    engine.externalTaskService.fetchAndLock(Int.MAX_VALUE, pattern.hash).topic(pattern.hash, Long.MAX_VALUE).execute().map { task ->
                        message.target(MessageTarget(definition.name, task.processInstanceId, pattern.hash))
                    },
                    engine.runtimeService.createEventSubscriptionQuery().eventType(EventType.MESSAGE.name()).eventName(pattern.hash).list().map { subscription ->
                        message.target(MessageTarget(definition.name, subscription.processInstanceId, pattern.hash))
                    }
                ).flatten()

            }.flatten()

        }.flatten()

        if (targets.isEmpty()) {
            log.debug("> Target: None")
        }
        return targets

    }

    fun correlate(message: Message<*>) {

        assert(message.target != null) { "Correlation only works for messages with a specified target!" }

        val flowName = message.target!!.first
        val flowDefinition = FlowDefinitions.getElementById(flowName) as FlowDefinition<*>
        val processInstanceId = message.target!!.second
        val correlationHash = message.target!!.third

        fun variables(): Map<String, Any> {

            val messages = if (processInstanceId != null) {
                val serialised = engine.runtimeService.getVariableTyped<JsonValue>(processInstanceId, MESSAGES_VAR, false)?.valueSerialized
                if (serialised != null) flowDefinition.deserialize(serialised) else emptyList()
            } else emptyList()

            with(messages.toMutableList()) {
                add(message)
                log.debug("> Target: ${flowDefinition.aggregate(map { it.fact })}")
                return mapOf(MESSAGES_VAR to SpinValues.jsonValue(flowDefinition.serialize(this)).create())
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

    fun <A: Aggregate> load(id: AggregateId, type: KClass<A>): A {

        val flowDefinition = FlowDefinitions.get(type)

        val processInstance =
            engine.historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(id)
                .singleResult()

        val messages = engine.historyService.createHistoricVariableInstanceQuery()
            .processInstanceId(processInstance.id)
            .variableName(MESSAGES_VAR)
            .disableCustomObjectDeserialization()
            .singleResult().value as JacksonJsonNode?

        if (messages != null) {
            return flowDefinition.aggregate(flowDefinition.deserialize(messages.unwrap()).map { it.fact })
        }

        throw IllegalArgumentException()

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

        val message = FlowDefinitions.deserialize(configuration!!.message)
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
        FlowDefinitions.all().forEach { flowDefinition ->
            val bpmn = transform(translate(flowDefinition))
            engine.repositoryService
                .createDeployment()
                .addModelInstance("${flowDefinition.name}.bpmn", bpmn)
                .name(flowDefinition.name)
                .deploy()
        }
    }

}