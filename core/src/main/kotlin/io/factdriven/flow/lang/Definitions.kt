package io.factdriven.flow.lang

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.full.memberFunctions

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias NodeName = String
typealias NodeId = String

interface Node {

    val id: NodeId
        get() {
            val id = StringBuffer()
            if (parent != null) {
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

    val name: FactName
    val parent: DefinedFlow<*>?
    val root: DefinedFlow<*>
        get() {
            return parent?.root ?: this as DefinedFlow<*>
        }

}

interface DefinedFlow<ENTITY: Entity>: Node {

    val children: List<Node>
    val classifier: FlowClassifier
    val entityType: EntityType<ENTITY>

    fun patterns(message: Fact): MessagePatterns {

        val patterns = mutableSetOf<MessagePattern>()

        children.forEach { child ->
            when(child) {
                is DefinedMessageReaction -> if (child.factType.isInstance(message)) patterns.add(child.incoming(message))
                is DefinedFlow<*> -> patterns.addAll(child.patterns(message))
            }
        }

        return patterns

    }

    val descendants: List<Node> get() {

        val descendants = mutableSetOf<Node>()

        descendants.add(this)
        children.forEach { child ->
            when(child) {
                is DefinedFlow<*> -> descendants.addAll(child.descendants)
                else -> {
                    descendants.add(child)
                    if (child is DefinedReaction) child.action?.let { descendants.add(it) }
                }
            }
        }

        return descendants.toList()

    }

    val childrenMap: Map<NodeId, Node> get() = children.map { it.id to it }.toMap()

    val nodes: Map<NodeId, Node> get() = descendants.map { it.id to it }.toMap()

    fun messageType(messageName: FactName): FactType<*>? {

        descendants.forEach {
            when(it) {
                is DefinedAction -> if (it.name == messageName && it.factType != null) return it.factType
                is DefinedReactionAction -> if (it.name == messageName && it.factType != null) return it.factType
                is DefinedMessageReaction -> if (it.name == messageName) return it.factType
            }
        }

        return null

    }

    fun getChildByActionType(actionClassifier: ActionClassifier): Node? {
        return children.find {
            when (it) {
                is DefinedAction -> it.classifier == actionClassifier
                is DefinedReaction -> it.action?.classifier == actionClassifier
                else -> false
            }
        }
    }

}

interface DefinedAction: Node {

    val classifier: ActionClassifier
    val factType: FactType<*>?
    val function: (Entity.() -> Fact)?

}

interface DefinedReactionAction: Node {

    val classifier: ActionClassifier
    val factType: FactType<*>?
    val function: (Entity.(Fact) -> Fact)?

}

interface DefinedReaction: Node {

    val classifier: ReactionClassifier
    val action: DefinedReactionAction?

}

interface DefinedMessageReaction: DefinedReaction {

    val factType: FactType<*>
    val properties: List<Property>
    val values: List<Entity?.() -> Fact?>

    fun incoming(message: Fact): MessagePattern {

        assert(factType.isInstance(message))

        val properties = properties.map { propertyName ->
            propertyName to message.getValue(propertyName)
        }.toMap()

        return MessagePattern(factType, properties)

    }

    fun expected(aggregate: Entity?): MessagePattern {

        val properties = properties.mapIndexed { propertyIndex, propertyName ->
            propertyName to values[propertyIndex].invoke(aggregate)
        }.toMap()

        return MessagePattern(factType, properties)

    }

}
