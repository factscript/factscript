package org.factscript.language.execution.cam

import org.factscript.execution.Message
import org.factscript.language.*
import org.factscript.execution.Messages
import org.factscript.execution.MessageId
import org.factscript.execution.Receptor
import org.factscript.language.definition.*
import org.factscript.language.visualization.bpmn.model.asBpmnId
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.delegate.Expression
import org.camunda.spin.plugin.variable.value.JsonValue

class EngineTransitionListener: ExecutionListener {

    private lateinit var target: Expression

    override fun notify(execution: DelegateExecution) {

        val nodeId = target.getValue(execution).toString()
        val node = Flows.get(nodeId).get(nodeId)
        val messageString = execution.getVariableTyped<JsonValue>(MESSAGES_VAR, false).valueSerialized
        val messages = Messages.fromJson(messageString)

        when (node) {
            is Correlating -> node.endpoints(messages)
            is Executing -> node.endpoints(messages)
            is Branching -> node.endpoints(messages)
            is Throwing -> node.endpoints(messages) // Compensation
            else -> emptyMap()
        }.forEach {
            execution.setVariable(it.key.asBpmnId(), it.value)
        }

    }

    private fun Correlating.endpoints(messages: List<Message>): Map<String, String> {
        val instance = Messages.load(messages, entity)
        val details = correlating.map { it.key to it.value.invoke(instance) }.toMap()
        val successfulEndpoint = mapOf(id to Receptor(consuming, details).hash)
        return successfulEndpoint + optionalEndpoints(instance)
    }

    private fun Executing.endpoints(messages: List<Message>): Map<String, String> {
        val successfulEndpoint = mapOf(id to Receptor(correlating = MessageId.nextAfter(messages.last().id)).hash)
        return successfulEndpoint + optionalEndpoints(factory)
    }

    private fun Branching.endpoints(messages: List<Message>): Map<String, String> {
        val instance = Messages.load(messages, entity)
        return optionalEndpoints(instance)
    }

    private fun Throwing.endpoints(messages: List<Message>): Map<String, String> {
        if (isCompensating()) {
            return root.descendants.filter { (it as? Executing)?.isCompensating() == true }.map {
                (it as Executing).endpoints(messages)
            }.map { it.map { it.key to it.value } }.flatten().toMap()
        } else {
            return emptyMap()
        }
    }

    private fun Node.optionalEndpoints(instance: Any): Map<String, String> {
        return children.mapNotNull {
            val correlating = it.children.first()
            if (correlating is Correlating) {
                val details = correlating.correlating.map { it.key to it.value.invoke(instance) }.toMap()
                correlating.id to Receptor(correlating.consuming, details).hash
            } else if (correlating is Waiting) {
                val timer = correlating.period?.invoke(instance) // TODO ?: awaiting.limit?.invoke(handlerInstance)
                correlating.id to timer!!
            } else null
        }.toMap()
    }

}