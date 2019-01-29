package io.factdriven.flow.lang

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias Aggregate = Any
typealias AggregateId = String
typealias AggregateName = String
typealias AggregateType = KClass<*>
typealias AggregateIds = List<AggregateId>

typealias ElementName = String
typealias ElementId = String

interface FlowElement {

    val id: ElementId
        get() {
            val id = StringBuffer()
            if (parent != null && parent!!.parent != null) {
                id.append(parent!!.id)
                id.append("-")
            }
            id.append(name)
            if (parent != null) {
                id.append("-")
                val idx = parent!!.children.indexOf(this)
                val counter = parent!!.children.count {
                    it.name == name && parent!!.children.indexOf(it) <= idx
                }
                id.append(counter)
            }
            return id.toString()
        }

    val name: ElementName
    val parent: FlowDefinition<*>?

}

object FlowDefinitions {

    private val list = mutableListOf<FlowDefinition<*>>()

    fun add(definition: FlowDefinition<*>) {
        list.add(definition)
    }

    fun all(): List<FlowDefinition<*>> {
        return list
    }

    fun <A: Aggregate> get(type: KClass<A>): FlowDefinition<A> {
        val definition = list.find {
            it.aggregateType == type
        } as FlowDefinition<A>?
        return definition ?: throw IllegalArgumentException()
    }

    fun get(name: ElementName): FlowDefinition<*> {
        val definition = list.find {
            it.name == name
        }
        return definition ?: throw IllegalArgumentException()
    }

}

interface FlowDefinition<A: Aggregate>: FlowElement {

    val children: List<FlowElement>
    val executionType: FlowExecutionType
    val aggregateType: AggregateType

    fun patterns(message: Fact): MessagePatterns {

        val patterns = mutableSetOf<MessagePattern>()

        children.forEach { child ->
            when(child) {
                is FlowMessageReactionDefinition -> if (child.messageType.isInstance(message)) patterns.add(child.incoming(message))
                is FlowDefinition<*> -> patterns.addAll(child.patterns(message))
            }
        }

        return patterns

    }

    val descendants: List<FlowElement> get() {

        val descendants = mutableListOf<FlowElement>()

        children.forEach { child ->
            descendants.add(child)
            if (child is FlowDefinition<*>) {
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
        return deserialize(jacksonObjectMapper().readTree(stream))
    }

    fun deserialize(stream: JsonNode): List<Message<*>> {
        return stream.map {
            Message.fromJson(it, messageType(it.get("name").textValue())!!)
        }
    }

    fun serialize(messages: List<Message<*>>): String {
        return jacksonObjectMapper().writeValueAsString(messages)
    }

    fun aggregate(history: Messages): A {

        assert(!history.isEmpty())

        fun past(history: Messages, aggregate: A): A {
            if (!history.isEmpty()) {
                val message = history.first()
                val method = aggregate::class.memberFunctions.find { it.parameters.size == 2 && it.parameters[1].type.classifier == message::class }
                if (method != null) method.call(aggregate, message)
                return past(history.subList(1, history.size), aggregate)
            } else {
                return aggregate
            }
        }

        val message = history.first()
        val constructor = aggregateType.constructors.find { it.parameters.size == 1 && it.parameters[0].type.classifier == message::class }
        return if (constructor != null) {
            past(history.subList(1, history.size), constructor.call(message) as A)
        } else throw IllegalArgumentException()

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
