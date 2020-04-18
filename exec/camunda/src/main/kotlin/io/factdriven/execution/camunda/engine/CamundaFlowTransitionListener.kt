package io.factdriven.execution.camunda.engine

import io.factdriven.Flows
import io.factdriven.Messages
import io.factdriven.definition.Awaiting
import io.factdriven.definition.Executing
import io.factdriven.execution.MessageId
import io.factdriven.execution.Receptor
import io.factdriven.execution.camunda.model.toMessageName
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.delegate.Expression
import org.camunda.spin.plugin.variable.value.JsonValue

class CamundaFlowTransitionListener: ExecutionListener {

    private lateinit var target: Expression

    override fun notify(execution: DelegateExecution) {

        val nodeId = target.getValue(execution).toString()
        val handling = when (val node = Flows.get(nodeId).get(nodeId)) {
            is Awaiting -> mapOf(nodeId to node.endpoint(execution))
            is Executing -> mapOf(nodeId to node.endpoint(execution))
            else -> emptyMap()
        }
        handling.forEach {
            execution.setVariable(it.key.toMessageName(), it.value.hash)
        }

    }

    private fun Awaiting.endpoint(execution: DelegateExecution): Receptor {
        val messageString = execution.getVariableTyped<JsonValue>(
            MESSAGES_VAR, false).valueSerialized
        val messages = Messages.fromJson(messageString)
        val handlerInstance = Messages.load(messages, entity)
        val details = properties.mapIndexed { propertyIndex, propertyName ->
            propertyName to matching[propertyIndex].invoke(handlerInstance)
        }.toMap()
        return Receptor(catching, details)
    }

    private fun Executing.endpoint(execution: DelegateExecution): Receptor {
        val messageString = execution.getVariableTyped<JsonValue>(
            MESSAGES_VAR, false).valueSerialized
        val messages = Messages.fromJson(messageString)
        return Receptor(
            catching,
            MessageId.nextAfter(messages.last().id)
        )
    }

}