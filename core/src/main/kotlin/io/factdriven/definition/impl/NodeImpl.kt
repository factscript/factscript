package io.factdriven.definition.impl

import io.factdriven.definition.api.*
import io.factdriven.execution.type
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class NodeImpl(override val parent: Node?, override val entity: KClass<*> = parent!!.entity): Node {

    override val children: MutableList<Node> = mutableListOf()

    override val id: String get() = id()
    override val label: String get() = label()

    override val index: Int get() = index()

    override val first: Node get() = first()
    override val last: Node get() = last()
    override val previous: Node? get() = previous()
    override val next: Node? get() = next()

    override fun findCatching(catching: KClass<*>): Catching? {
        return children.find { it is Catching && it.catching == catching } as Catching?
    }

    override fun findConsuming(consuming: KClass<*>): Consuming? {
        return children.find { it is Consuming && it.catching == consuming } as Consuming?
    }

    override fun findThrowing(throwing: KClass<*>): Throwing? {
        return children.find { it is Throwing && it.throwing == throwing } as Throwing?
    }

    override fun findExecuting(executing: KClass<*>): Executing? {
        return children.find { it is Throwing && it.throwing == executing } as Executing?
    }

    override fun findPromising(promising: KClass<*>): Promising? {
        return children.find { it is Promising && it.succeeding != null && promising.isSuperclassOf(it.succeeding!!) } as Promising?
    }

    override fun <N: Node> findById(id: String, type: KClass<in N>): N? {
        if (type.isInstance(this) && this.id == id)
            return this as N
        children.forEach {
            val node = it.findById(id, type)
            if (node != null)
                return node
        }
        return null
    }

    override fun isFirst(): Boolean = this == first

    override fun isLast(): Boolean = this == last

}

private fun Node.id(): String {
    val id = StringBuffer(parent?.id ?: entity.type.context)
    id.append("-")
    id.append(entity.type.local)
    if (parent != null)
        id.append("-").append(parent?.children?.count {
            it.entity.type == entity.type && it.index <= index
        } ?: 0)
    return id.toString()
}

private fun Node.label(): String {
    return entity.type.toLabel()
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
    return  if (!isFirst()) parent!!.children[index - 1] else null
}

private fun Node.next(): Node? {
    return if (!isLast()) parent!!.children[index + 1] else null
}
