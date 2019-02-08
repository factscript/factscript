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

    fun match(message: Message<*>): MessagePatterns = emptyList()

}

interface Flow<ENTITY: Entity>: Node {

    val children: List<Node>
    val nodes: Map<NodeId, Node> get() = nodes()

    val classifier: FlowClassifier
    val type: EntityType<ENTITY>

    override fun match(message: Message<*>): MessagePatterns {
        return mutableListOf<MessagePattern>().let { children.forEach { child -> it.addAll(child.match(message)) }; it }
    }

    fun getNode(classifier: ActionClassifier): Node? {
        return children.find {
            when (it) {
                is Action -> it.classifier == classifier
                is Reaction -> it.action?.classifier == classifier
                else -> false
            }
        }
    }

    companion object {

        fun getNode(id: NodeId): Node {
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

    override fun match(message: Message<*>): MessagePatterns {

        return if (type.isInstance(message.fact)) {
            listOf(MessagePattern(root.type, type, properties.map {
                propertyName -> propertyName to message.fact.getValue(propertyName)
            }.toMap()))
        } else {
            emptyList()
        }

    }

    fun match(any: Entity?): MessagePatterns {

        val properties = properties.mapIndexed { propertyIndex, propertyName ->
            propertyName to values[propertyIndex].invoke(any)
        }.toMap()

        return listOf(MessagePattern(root.type, type, properties))

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
