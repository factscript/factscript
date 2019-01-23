package io.factdriven.flow.lang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias Aggregate = Any
typealias AggregateId = String
typealias AggregateType = KClass<out Aggregate>
typealias AggregateIds = List<AggregateId>

typealias FlowElementType = String
typealias FlowElementId = String

interface FlowElement {

    val flowElementId: FlowElementId
        get() = (container?.flowElementId ?: "") + (if (container != null) "-" else "") + flowElementType

    val flowElementType: FlowElementType
    val container: FlowDefinition?

}

interface FlowDefinition: FlowElement {

    val flowElements: MutableList<FlowElement>
    val flowExecutionType: FlowExecutionType
    val aggregateType: AggregateType

}

interface FlowActionDefinition: FlowElement {

    val flowActionType: FlowActionType
    val function: (Aggregate.() -> Message)?

}

interface FlowReactionActionDefinition: FlowElement {

    val flowActionType: FlowActionType
    val function: (Aggregate.(Any) -> Message)?

}

interface FlowReactionDefinition: FlowElement {

    val flowReactionType: FlowReactionType
    val flowReactionAction: FlowReactionActionDefinition

}

interface FlowMessageReactionDefinition: FlowReactionDefinition {

    val type: KClass<out Message>
    val keys: List<PropertyName>
    val values: List<Aggregate.() -> Any?>

}
