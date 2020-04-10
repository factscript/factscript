package io.factdriven.implementation

import io.factdriven.definition.*
import io.factdriven.execution.Receptor
import io.factdriven.execution.Message
import io.factdriven.execution.label
import io.factdriven.execution.type
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class NodeImpl(override val parent: Node?, override val entity: KClass<*> = parent!!.entity):
    Node {

    override val children: MutableList<Node> = mutableListOf()

    override val id: String get() = id()
    override val label: String get() = label()

    override val position: Int get() = index()

    override val first: Node get() = first()
    override val last: Node get() = last()
    override val previous: Node? get() = previous()
    override val next: Node? get() = next()

    @Suppress("UNCHECKED_CAST")
    override fun <N: Node> find(nodeOfType: KClass<N>, dealingWith: KClass<*>?): N? =
        when {
            nodeOfType.isSubclassOf(Throwing::class) -> {
                children.find { it is Throwing && (it.throwing == dealingWith || dealingWith == null)}
            }
            nodeOfType.isSubclassOf(Promising::class) -> {
                children.find { it is Promising && (it.succeeding == dealingWith || dealingWith == null) }
            }
            else -> {
                children.find { it is Catching && (it.catching == dealingWith || dealingWith == null) }
            }
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
            else -> {
                children.filter { it is Catching && (it.catching == dealingWith || dealingWith == null) }
            }
        } as List<N>

    override fun get(id: String): Node {
        return get(id, Node::class)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <N: Node> get(id: String, type: KClass<in N>): N =
        (if (type.isInstance(this) && this.id == id)
            this
        else
            children.find { type.isInstance(it) && it.id == id } ?: throw IllegalArgumentException("Node '${id}' is not defined!")) as N

    override fun isFirst(): Boolean = this == first

    override fun isLast(): Boolean = this == last

    override fun findReceptorsFor(message: Message): List<Receptor> {
        return children.map { it.findReceptorsFor(message) }.flatten()
    }

}

private fun Node.id(): String {
    val id = StringBuffer(parent?.id ?: entity.type.context)
    id.append("-")
    id.append(entity.type.local)
    if (parent != null)
        id.append("-").append(parent?.children?.count {
            it.entity.type == entity.type && it.position <= position
        } ?: 0)
    return id.toString()
}

private fun Node.label(): String {
    return entity.type.label
}

private fun Node.index(): Int {
    return parent?.children?.indexOf(this) ?: 0
}

private fun Node.first(): Node {
    return if (parent != null) parent!!.children.first() else this
}

private fun Node.last(): Node {
    return if (parent != null) parent!!.children.last() else this
}

private fun Node.previous(): Node? {
    return  if (!isFirst()) parent!!.children[position - 1] else null
}

private fun Node.next(): Node? {
    return if (!isLast()) parent!!.children[position + 1] else null
}
