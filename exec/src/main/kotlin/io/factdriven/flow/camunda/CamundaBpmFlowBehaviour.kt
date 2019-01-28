package io.factdriven.flow.camunda

import io.factdriven.flow.lang.*
import io.factdriven.flow.past
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.impl.event.EventType
import org.camunda.spin.plugin.variable.SpinValues
import org.camunda.spin.plugin.variable.value.JsonValue

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
object CamundaBpmFlowBehaviour: JavaDelegate {

    override fun execute(execution: DelegateExecution) {

        val processDefinitionKey = execution.getVariable("processDefinitionKey") as String
        val flowDefinition = FlowDefinitions.get(processDefinitionKey)
        val element = flowDefinition.descendantMap[execution.currentActivityId]!!
        val messages = flowDefinition.deserialize(execution.getVariableTyped<JsonValue>("messages", false).valueSerialized!!).toMutableList()

        fun aggregate() = past(messages.map { it.fact }, flowDefinition.aggregateType)!!

        fun message(element: FlowElement): Message<*>? {
            return when(element) {
                is FlowActionDefinition -> {
                    val action = element.function
                    if (action != null) Message.from(action.invoke(aggregate())) else null
                }
                is FlowMessageReactionDefinition -> {
                    val action = element.action?.function
                    if (action != null) Message.from(action.invoke(aggregate(), messages.last().fact)) else null
                }
                is FlowDefinition -> {
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
                is FlowDefinition -> {
                    pattern(element.children.last())
                }
                else -> null
            }
        }

        val message = message(element)
        if (message != null) {
            println("Outgoing $message") // TODO
            messages.add(message)
        }

        val pattern = pattern(element)

        execution.variables = mapOf(
            "messages" to SpinValues.jsonValue(flowDefinition.serialize(messages)),
            "data" to pattern?.hash
        )

    }

}

object CamundaBpmFlowExecutor {

    lateinit var engine: ProcessEngine

    fun <F: Fact> target(message: Message<F>) : List<Message<F>> {

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

        val flowDefinition = FlowDefinitions.get(message.target!!.first)
        val processInstanceId = message.target!!.second
        val correlationHash = message.target!!.third

        fun variables(): Map<String, Any> {

            val messages = if (processInstanceId != null) {
                val serialised = engine.runtimeService.getVariableTyped<JsonValue>(processInstanceId, "messages", false)?.valueSerialized
                if (serialised != null) flowDefinition.deserialize(serialised) else emptyList()
            } else emptyList()

            with(messages.toMutableList()) {
                add(message)
                println("Incoming $message") // TODO
                return mapOf(
                    "processDefinitionKey" to flowDefinition.name,
                    "messages" to SpinValues.jsonValue(flowDefinition.serialize(this)).create()
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
        if (processInstanceId != null)
            correlationBuilder.processInstanceId(processInstanceId)
        correlationBuilder.correlate()

    }

}
