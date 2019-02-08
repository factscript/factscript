package io.factdriven.flow.lang

import io.factdriven.flow.Flows

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias NodeName = String
typealias NodeId = String

interface Node {

    val name: FactName
    val id: NodeId get() = id()
    val parent: Flow<*>?
    val root: Flow<*> get() = parent?.root ?: this as Flow<*>

}

interface Flow<ENTITY: Entity>: Node {

    val children: List<Node>
    val nodes: Map<NodeId, Node> get() = nodes()

    val classifier: FlowClassifier
    val type: EntityType<ENTITY>

    fun match(message: Message<*>): MessagePatterns {

        val patterns = mutableSetOf<MessagePattern>()

        children.forEach { child ->
            when(child) {
                is MessageReaction -> child.match(message)?.let { patterns.add(it) }
                is Flow<*> -> patterns.addAll(child.match(message))
            }
        }

        return patterns

    }

    fun getChildByActionType(actionClassifier: ActionClassifier): Node? {
        return children.find {
            when (it) {
                is Action -> it.classifier == actionClassifier
                is Reaction -> it.action?.classifier == actionClassifier
                else -> false
            }
        }
    }

    companion object {

        fun node(id: NodeId): Node {
            return Flows.get(id).nodes[id] ?: throw java.lang.IllegalArgumentException()
        }

    }

}

interface Action: Node {

    val classifier: ActionClassifier
    val type: FactType<*>?
    val function: (Entity.() -> Fact)?

}

interface ReactionAction: Node {

    val classifier: ActionClassifier
    val type: FactType<*>?
    val function: (Entity.(Fact) -> Fact)?

}

interface Reaction: Node {

    val classifier: ReactionClassifier
    val action: ReactionAction?

}

interface MessageReaction: Reaction {

    val type: FactType<*>
    val properties: List<Property>
    val values: List<Entity?.() -> Fact?>

    fun match(message: Message<*>): MessagePattern? {

        if (type.isInstance(message.fact)) {

            val properties = properties.map { propertyName ->
                propertyName to message.fact.getValue(propertyName)
            }.toMap()

            return MessagePattern(root.type, type, properties)
        } else {
            return null
        }

    }

    fun expected(aggregate: Entity?): MessagePattern {

        val properties = properties.mapIndexed { propertyIndex, propertyName ->
            propertyName to values[propertyIndex].invoke(aggregate)
        }.toMap()

        return MessagePattern(root.type, type, properties)

    }

}

private fun Node.id(): NodeId {
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


private fun Flow<*>.nodes(): Map<NodeId, Node> {

    val descendants = mutableMapOf<NodeId, Node>()

    descendants[id] = this
    children.forEach { child ->
        when(child) {
            is Flow<*> -> descendants.putAll(child.nodes())
            else -> {
                descendants[child.id] = child
                if (child is Reaction) child.action?.let { descendants[it.id] = it }
            }
        }
    }

    return descendants

}
