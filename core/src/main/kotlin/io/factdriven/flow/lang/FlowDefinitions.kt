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
        get() = (parent?.flowElementId ?: "") + (if (parent != null) "-" else "") + flowElementType

    val flowElementType: FlowElementType
    val parent: FlowDefinition?

}

interface FlowDefinition: FlowElement {

    val children: List<FlowElement>
    val flowExecutionType: FlowExecutionType
    val aggregateType: AggregateType

    fun patterns(message: Message): MessagePatterns {

        val patterns = mutableSetOf<MessagePattern>()

        children.forEach { child ->
            when(child) {
                is FlowMessageReactionDefinition -> if (child.type.isInstance(message)) patterns.add(child.pattern(message))
                is FlowDefinition -> patterns.addAll(child.patterns(message))
            }
        }

        return patterns

    }

    val descendants: List<FlowElement> get() {

        val descendants = mutableListOf<FlowElement>()

        children.forEach { child ->
            descendants.add(child)
            if (child is FlowDefinition) {
                descendants.addAll(child.descendants)
            }
        }

        return descendants

    }

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
    val propertyNames: List<PropertyName>
    val propertyValues: List<Aggregate.() -> Any?>

    fun pattern(message: Message): MessagePattern {

        assert(type.isInstance(message))

        val properties = propertyNames.map { propertyName ->
            propertyName to message.getProperty(propertyName)
        }.toMap()

        return MessagePattern(type, properties)

    }

}
