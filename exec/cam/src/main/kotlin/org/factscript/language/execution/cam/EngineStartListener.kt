package org.factscript.language.execution.cam

import org.factscript.execution.*
import org.factscript.language.definition.Node
import org.factscript.language.definition.Promising
import org.factscript.language.definition.Throwing
import org.factscript.language.impl.utils.Json
import org.factscript.language.impl.utils.prettyJson
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.spin.plugin.variable.SpinValues
import org.camunda.spin.plugin.variable.value.JsonValue
import org.slf4j.*

class EngineStartListener: ExecutionListener {

    override fun notify(execution: DelegateExecution) {

        if (!execution.currentActivityId.contains("Compensate")
            && !execution.currentActivityId.contains("Fork")
            && !execution.currentActivityId.contains("Join")) {

            val node = execution.node
            val messages = Messages.fromJson(Json(execution.getVariableTyped<JsonValue>(MESSAGES_VAR, false).valueSerialized!!)).toMutableList()
            fun aggregate() = messages.newInstance(node.entity)

            fun message(node: Node): Message? {
                return when(node) {
                    is Throwing -> {
                        val fact = node.factory.invoke(aggregate())
                        val correlating = execution.flow.find(nodeOfType = Promising::class, dealingWith = fact::class)
                        Message(messages, Fact(fact), if (correlating != null) messages.findLast { it.fact.type.kClass == correlating.consuming }!!.correlating else null)
                    }
                    else -> null
                }
            }

            message(node)?.let {
                messages.add(it)
                execution.setVariable(MESSAGES_VAR, SpinValues.jsonValue(messages.prettyJson))
                Messages.publish(it)
            }

        }

    }

}