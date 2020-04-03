package io.factdriven.definition.api

import io.factdriven.execution.Type
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Node {

    val id: String
    val type: Type
    val label: String

    val entityType: KClass<*>

    val index: Int
    val parent: Node?
    val children: List<Node>

    fun isFirst(): Boolean = this == first
    fun isLast(): Boolean = this == last

    val first: Node get() = if (parent != null) parent!!.children.first() else this
    val last: Node get() = if (parent != null) parent!!.children.last() else this
    val previous: Node? get() = if (!isFirst()) parent!!.children[index - 1] else null
    val next: Node? get() = if (!isLast()) parent!!.children[index + 1] else null

}