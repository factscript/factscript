package io.factdriven.flow.camunda

import io.factdriven.flow.lang.*
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior
import org.camunda.bpm.engine.impl.event.EventType
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution
import org.camunda.spin.plugin.variable.SpinValues
import org.camunda.spin.plugin.variable.value.JsonValue

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

object FlowServiceBehaviour : AbstractBpmnActivityBehavior() {

    override fun execute(execution: ActivityExecution) {
        //
    }

    override fun signal(execution: ActivityExecution, signalName: String?, signalData: Any?) {
        leave(execution)
        // propagateBpmnError(if (signalData !is String) BpmnError(signalName) else BpmnError(signalName, signalData), execution)
    }

}

object FlowActionBehaviour : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        //
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

        fun messages(): JsonValue {

            val messages = if (processInstanceId != null) {
                val serialised = engine.runtimeService.getVariableTyped<JsonValue>(processInstanceId, "messages", false)?.valueSerialized
                if (serialised != null) flowDefinition.deserialize(serialised) else emptyList()
            } else emptyList()

            with(messages.toMutableList()) {
                add(message)
                return SpinValues.jsonValue(flowDefinition.serialize(this)).create()
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
                    mapOf("messages" to messages())
                )
                return
            }

        }

        val correlationBuilder = engine.runtimeService.createMessageCorrelation(correlationHash)
            .setVariable("messages", messages())
        if (processInstanceId != null)
            correlationBuilder.processInstanceId(processInstanceId)
        correlationBuilder.correlate()

    }

}
