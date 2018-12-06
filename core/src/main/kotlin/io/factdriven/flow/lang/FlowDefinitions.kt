package io.factdriven.flow.lang

import io.factdriven.flow.FlowMessage

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

interface FlowElement {

    val name: String

}

interface FlowExecutionDefinition: FlowElement {

    val executionType: FlowExecutionType
    val elements: MutableList<FlowElement>

}

interface FlowActionDefinition: FlowElement {

    val actionType: FlowActionType
    val function: (() -> FlowMessage)?

}

interface FlowReactionDefinition: FlowElement {

    var reactionType: FlowReactionType
    var actionType: FlowActionType
    var function: ((Any) -> FlowMessage)?

}

interface FlowMessageReactionDefinition: FlowReactionDefinition {

    val messagePattern: FlowMessagePattern<*>

}
