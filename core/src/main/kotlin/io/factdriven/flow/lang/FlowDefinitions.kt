package io.factdriven.flow.lang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias FlowInstance = Any
typealias FlowInstanceId = String
typealias FlowElementId = String
typealias FlowElementName = String

typealias FlowInstanceIds = List<FlowInstanceId>

interface FlowElement {

    // val id: FlowElementId
    val name: FlowElementName
    val parent: FlowDefinition?

}

interface FlowDefinition: FlowElement {

    val elements: MutableList<FlowElement>
    val executionType: FlowExecutionType
    val instanceType: KClass<out FlowInstance>

}

interface FlowActionDefinition: FlowElement {

    val type: FlowActionType
    val function: (FlowInstance.() -> FlowMessagePayload)?

}

interface FlowReactionActionDefinition: FlowElement {

    val type: FlowActionType
    val function: (FlowInstance.(Any) -> FlowMessagePayload)?

}

interface FlowReactionDefinition: FlowElement {

    val type: FlowReactionType
    val action: FlowReactionActionDefinition

}

interface FlowMessageReactionDefinition: FlowReactionDefinition {

    val payloadType: KClass<out FlowMessagePayload>
    val keys: List<FlowMessageProperty>
    val values: List<FlowInstance.() -> Any?>

}
