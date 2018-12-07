package io.factdriven.flow.lang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias FlowInstance = Any
typealias FlowInstanceId = String
typealias FlowDefinitionId = String

typealias FlowInstanceIds = List<FlowInstanceId>

interface FlowElement {

    val name: String

}

interface FlowDefinition: FlowElement {

    val executionType: FlowExecutionType
    val elements: MutableList<FlowElement>
    val instanceType: KClass<out FlowInstance>

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

    val messagePattern: FlowMessagePattern<out FlowMessage>

}
