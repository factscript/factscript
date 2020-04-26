package io.factdriven.language.impl.definition

import io.factdriven.language.definition.*
import io.factdriven.execution.*
import io.factdriven.language.impl.utils.Id
import io.factdriven.language.impl.utils.toSentenceCase
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class NodeImpl(override val parent: Node?, override val entity: KClass<*> = parent!!.entity):
    Node {

    override val children: MutableList<Node> = mutableListOf()

    @Suppress("LeakingThis")
    override val root: Flow get() = (parent?.root ?: this) as Flow

    override val id: String get() = id()

    override val type: Type get() = entity.type

    override val label: String get() = label()

    override val position: Int get() = index()

    override val firstSibling: Node get() = firstSibling()
    override val lastSibling: Node get() = lastSibling()
    override val previousSibling: Node? get() = previousSibling()
    override val nextSibling: Node? get() = nextSibling()

    override val start: Node get() = parent?.start ?: children.first()
    override val finish: Node get() = parent?.finish ?: children.last()
    override val forward: Node? get() = nextSibling ?: parent?.forward
    override val backward: Node? get() = previousSibling ?: parent?.backward

    protected fun id(): String {
        val parentId =  if (isChild()) parent!!.id else "${entity.type.context}${idSeparator}${entity.type.name}"
        val nodeTypeCount = if (isChild()) parent!!.children.count { it.type == type } else 0
        val nodeTypePos = if (nodeTypeCount > 1) "${positionSeparator}${parent!!.children.count { it.type == type && it.position <= position }}" else ""
        val id = if (isChild()) "${parentId}${idSeparator}${type.name}${nodeTypePos}" else parentId
        return Id(id)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <N: Node> find(nodeOfType: KClass<N>, dealingWith: KClass<*>?): N? =
        when {
            nodeOfType.isSubclassOf(Throwing::class) -> {
                children.find { it is Throwing && (it.throwing == dealingWith || dealingWith == null)}
            }
            nodeOfType.isSubclassOf(Promising::class) -> {
                children.find { it is Promising && (it.succeeding == dealingWith || dealingWith == null) }
            }
            nodeOfType.isSubclassOf(Catching::class) -> {
                children.find { it is Catching && (it.catching == dealingWith || dealingWith == null) }
            }
            else -> children.find { nodeOfType.isInstance(it) && dealingWith == null }
        } as N?

    @Suppress("UNCHECKED_CAST")
    override fun <N: Node> filter(nodesOfType: KClass<N>, dealingWith: KClass<*>?): List<N> =
        when {
            nodesOfType.isSubclassOf(Throwing::class) -> {
                children.filter { it is Throwing && (it.throwing == dealingWith || dealingWith == null)}
            }
            nodesOfType.isSubclassOf(Promising::class) -> {
                children.filter { it is Promising && (it.succeeding == dealingWith || dealingWith == null) }
            }
            nodesOfType.isSubclassOf(Catching::class) -> {
                children.filter { it is Catching && (it.catching == dealingWith || dealingWith == null) }
            }
            else -> children.filter { nodesOfType.isInstance(it) && dealingWith == null }
        } as List<N>

    override fun get(id: String): Node? {
        return get(id, Node::class)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <N: Node> get(id: String, type: KClass<in N>): N? {
        if (type.isInstance(this) && this.id == id)
            return this as N?
        else {
            children.forEach {
                val result = it.get(id, type)
                if (result != null)
                    return result
            }
        }
        return null
    }

    override fun isFirstSibling(): Boolean = this == firstSibling

    override fun isLastSibling(): Boolean = this == lastSibling

    override fun isParent() = children.isNotEmpty()

    override fun isRoot() = !isChild()

    override fun isChild() = parent != null

    override fun isStart() = isChild() && parent!!.isRoot() && isFirstSibling()

    override fun isFinish() = isChild() && parent!!.isRoot() && isLastSibling()

    override fun findReceptorsFor(message: Message): List<Receptor> {
        return children.map { it.findReceptorsFor(message) }.flatten()
    }

}

private fun Node.label(): String {
    return type.label.toSentenceCase()
}

private fun Node.index(): Int {
    return parent?.children?.indexOf(this) ?: 0
}

private fun Node.firstSibling(): Node {
    return parent?.children?.first() ?: this
}

private fun Node.lastSibling(): Node {
    return parent?.children?.last() ?: this
}

private fun Node.nextSibling(): Node? {
    return if (!isLastSibling()) parent!!.children[index() + 1] else null
}

private fun Node.previousSibling(): Node? {
    return  if (!isFirstSibling()) parent!!.children[index() - 1] else null
}

const val idSeparator = "-"
const val positionSeparator = "."