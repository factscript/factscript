package io.factdriven.definition.api

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Node {

    val id: String
    val entity: KClass<*>
    val label: String

    val parent: Node?
    val children: List<Node>
    val position: Int

    fun get(id: String): Node
    fun <N: Node> get(id: String, type: KClass<in N> = Node::class): N

    val first: Node
    val last: Node
    val previous: Node?
    val next: Node?

    fun isFirst(): Boolean
    fun isLast(): Boolean

    fun <N: Node> find(nodeOfType: KClass<N>, dealingWith: KClass<*>? = null): N?
    fun <N: Node> filter(nodesOfType: KClass<N>, dealingWith: KClass<*>? = null): List<N>

}
