package io.factdriven.flow.camunda

import io.factdriven.flow.lang.*
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngines
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.impl.ProcessEngineImpl
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
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity




/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
const val DEFINITION_NAME_VAR = "name"
const val MESSAGE_NAME_VAR = "message"
const val MESSAGES_VAR = "messages"

val log = LoggerFactory.getLogger(CamundaBpmFlowBehaviour::class.java)

object CamundaBpmFlowBehaviour: JavaDelegate {

    override fun execute(execution: DelegateExecution) {

        val definitionName = execution.getVariable(DEFINITION_NAME_VAR) as String
        val flowDefinition = FlowDefinitions.get(definitionName)
        val element = flowDefinition.descendantMap[execution.currentActivityId]!!
        val messages = flowDefinition.deserialize(execution.getVariableTyped<JsonValue>(MESSAGES_VAR, false).valueSerialized!!).toMutableList()

        fun aggregate() = flowDefinition.aggregate(messages.map { it.fact })!!

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
                    message(element.children.first()) // TODO properly retrieve intent creator
                }
                else -> throw IllegalArgumentException()
            }
        }

        fun pattern(element: FlowElement): MessagePattern? {
            return when (element) {
                is FlowMessageReactionDefinition -> {
                    element.expected(aggregate())
                }
                is FlowDefinition<*> -> {
                    pattern(element.children.last())
                }
                else -> null
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

        val pattern = pattern(element)

        execution.variables = mapOf(
            MESSAGES_VAR to SpinValues.jsonValue(flowDefinition.serialize(messages)),
            MESSAGE_NAME_VAR to pattern?.hash
        )

    }

}

object CamundaBpmFlowExecutor {

    val engine: ProcessEngine = ProcessEngines.getProcessEngines().values.first()

    fun target(message: String) : List<Message<*>> {
        return target(FlowDefinitions.deserialize(message))
    }

    fun <F: Fact> target(message: Message<F>) : List<Message<F>> {

        log.debug("Incoming: ${message.fact}")

        return FlowDefinitions.all().map { definition ->

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

    }

    fun correlate(message: Message<*>) {

        assert(message.target != null) { "Correlation only works for messages with a specified target!" }

        val flowName = message.target!!.first
        val flowDefinition = FlowDefinitions.get(flowName)
        val processInstanceId = message.target!!.second
        val correlationHash = message.target!!.third

        fun variables(): Map<String, Any> {

            val messages = if (processInstanceId != null) {
                val serialised = engine.runtimeService.getVariableTyped<JsonValue>(processInstanceId, MESSAGES_VAR, false)?.valueSerialized
                if (serialised != null) flowDefinition.deserialize(serialised) else emptyList()
            } else emptyList()

            with(messages.toMutableList()) {
                add(message)
                log.debug("> Target: ${flowName}")
                return mapOf(
                    DEFINITION_NAME_VAR to flowDefinition.name,
                    MESSAGES_VAR to SpinValues.jsonValue(flowDefinition.serialize(this)).create()
                )
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

        commandContext!!.processEngineConfiguration.commandExecutorSchemaOperations.execute<String> {
            Mocks.register("start", CamundaBpmFlowBehaviour)
            CamundaBpmFlowExecutor.target(configuration!!.message).forEach {
                CamundaBpmFlowExecutor.correlate(it)
            }
            ""
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