package org.factscript.language.execution.cam

import org.factscript.language.*
import org.factscript.execution.Messages
import org.factscript.language.definition.*
import org.factscript.execution.*
import org.factscript.language.impl.definition.positionSeparator
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.spin.plugin.variable.value.JsonValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val log: Logger = LoggerFactory.getLogger(Messages::class.java)

val DelegateExecution.history: List<Message> get() {
    return Messages.fromJson(getVariableTyped<JsonValue>(MESSAGES_VAR, false).valueSerialized)
}

val DelegateExecution.flow: Flow get() {
    return Flows.get(nodeId)
}

val DelegateExecution.node: Node get() {
    return node(nodeId)
}

val DelegateExecution.nodeId: String get() {
    return currentActivityId
        .replace("${positionSeparator}Fork", "")
        .replace("${positionSeparator}Join", "")
}

fun <N: Node> DelegateExecution.node(id: String): N {
    return flow.get(id) as N
}

val DelegateExecution.state: Any get() {
    return history.newInstance(node.entity)
}

const val MESSAGES_VAR = "messages"
