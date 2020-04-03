package io.factdriven.definition.api

import io.factdriven.execution.type
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Node {

    val id: String get() = id()
    val entity: KClass<*>
    val label: String get() = label()

    val parent: Node?
    val children: List<Node>
    val index: Int get() = index()

    val first: Node get() = first()
    val last: Node get() = last()
    val previous: Node? get() = previous()
    val next: Node? get() = next()

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

fun Node.isFirst(): Boolean = this == first

fun Node.isLast(): Boolean = this == last
