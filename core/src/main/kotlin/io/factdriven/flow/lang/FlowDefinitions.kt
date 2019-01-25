package io.factdriven.flow.lang

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias Aggregate = Any
typealias AggregateId = String
typealias AggregateType = KClass<*>
typealias AggregateIds = List<AggregateId>

typealias ElementName = String
typealias ElementId = String

interface FlowElement {

    val id: ElementId
        get() = (parent?.id ?: "") + (if (parent != null) "-" else "") + name

    val name: ElementName
    val parent: FlowDefinition?

}

interface FlowDefinition: FlowElement {

    val children: List<FlowElement>
    val executionType: FlowExecutionType
    val aggregateType: AggregateType

    fun patterns(message: Fact): MessagePatterns {

        val patterns = mutableSetOf<MessagePattern>()

        children.forEach { child ->
            when(child) {
                is FlowMessageReactionDefinition -> if (child.messageType.isInstance(message)) patterns.add(child.incoming(message))
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
            if (child is FlowReactionDefinition && child.action != null) {
                descendants.add(child.action!!)
            }
        }

        return descendants

    }

    val childrenMap: Map<ElementId, FlowElement> get() = children.map { it.id to it }.toMap()

    val descendantMap: Map<ElementId, FlowElement> get() = descendants.map { it.id to it }.toMap()

    fun messageType(messageName: FactName): FactType<*>? {

        descendants.forEach {
            when(it) {
                is FlowActionDefinition -> if (it.name == messageName && it.messageType != null) return it.messageType
                is FlowReactionActionDefinition -> if (it.name == messageName && it.messageType != null) return it.messageType
                is FlowMessageReactionDefinition -> if (it.name == messageName) return it.messageType
            }
        }

        return null

    }

    fun deserialize(stream: String): List<Message<*>> {
        return jacksonObjectMapper().readTree(stream).map {
            Message.fromJson(it, messageType(it.get("name").textValue())!!)
        }
    }

    fun serialize(messages: List<Message<*>>): String {
        return jacksonObjectMapper().writeValueAsString(messages)
    }

}

interface FlowActionDefinition: FlowElement {

    val actionType: FlowActionType
    val messageType: FactType<*>?
    val function: (Aggregate.() -> Fact)?

}

interface FlowReactionActionDefinition: FlowElement {

    val actionType: FlowActionType
    val messageType: FactType<*>?
    val function: (Aggregate.(Fact) -> Fact)?

}

interface FlowReactionDefinition: FlowElement {

    val reactionType: FlowReactionType
    val action: FlowReactionActionDefinition?

}

interface FlowMessageReactionDefinition: FlowReactionDefinition {

    val messageType: FactType<*>
    val propertyNames: List<PropertyName>
    val propertyValues: List<Aggregate?.() -> Any?>

    fun incoming(message: Fact): MessagePattern {

        assert(messageType.isInstance(message))

        val properties = propertyNames.map { propertyName ->
            propertyName to message.getProperty(propertyName)
        }.toMap()

        return MessagePattern(messageType, properties)

    }

    fun expected(aggregate: Aggregate?): MessagePattern {

        val properties = propertyNames.mapIndexed { propertyIndex, propertyName ->
            propertyName to propertyValues[propertyIndex].invoke(aggregate)
        }.toMap()

        return MessagePattern(messageType, properties)

    }

}
