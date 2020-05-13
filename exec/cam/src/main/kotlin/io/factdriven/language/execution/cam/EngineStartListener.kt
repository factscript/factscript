package io.factdriven.language.execution.cam

import io.factdriven.execution.*
import io.factdriven.language.definition.Node
import io.factdriven.language.definition.Promising
import io.factdriven.language.definition.Throwing
import io.factdriven.language.impl.utils.Json
import io.factdriven.language.impl.utils.prettyJson
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.spin.plugin.variable.SpinValues
import org.camunda.spin.plugin.variable.value.JsonValue

class EngineStartListener: ExecutionListener {

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
                    val fact = node.factory.invoke(aggregate())
                    val correlating = execution.flow.find(nodeOfType = Promising::class, dealingWith = fact::class)
                    Message(
                        messages,
                        Fact(fact),
                        if (correlating != null) messages.findLast { it.fact.type.kClass == correlating.consuming }!!.id else null
                    )
                }
                else -> null
            }
        }

        message(node)?.let {
            messages.add(it)
            execution.setVariable(
                MESSAGES_VAR,
                SpinValues.jsonValue(messages.prettyJson)
            )
            Messages.publish(it)
        }

    }

}