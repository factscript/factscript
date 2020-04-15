package io.factdriven.execution.camunda.engine

import io.factdriven.Messages
import io.factdriven.definition.Node
import io.factdriven.definition.Promising
import io.factdriven.definition.Throwing
import io.factdriven.execution.Fact
import io.factdriven.execution.Message
import io.factdriven.execution.newInstance
import io.factdriven.impl.utils.Json
import io.factdriven.impl.utils.json
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.spin.plugin.variable.SpinValues
import org.camunda.spin.plugin.variable.value.JsonValue

class CamundaFlowNodeStartListener: ExecutionListener {

    override fun notify(execution: DelegateExecution) {

        val node = execution.node
        val messages = Messages.fromJson(
            Json(
                execution.getVariableTyped<JsonValue>(
                    MESSAGES_VAR,
                    false
                ).valueSerialized!!
            )
        ).toMutableList()
        fun aggregate() = messages.newInstance(node.entity)

        fun message(node: Node): Message? {
            return when(node) {
                is Throwing -> {
                    val fact = node.instance.invoke(aggregate())
                    val correlating = execution.flow.find(nodeOfType = Promising::class)?.succeeding?.isInstance(fact) ?: false
                    Message(
                        messages,
                        Fact(fact),
                        if (correlating) messages.first().id else null
                    )
                }
                else -> null
            }
        }

        message(node)?.let {
            messages.add(it)
            execution.setVariable(
                MESSAGES_VAR,
                SpinValues.jsonValue(messages.json)
            )
            Messages.publish(it)
        }

    }

}