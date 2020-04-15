package io.factdriven.definition

import io.factdriven.execution.Receptor
import io.factdriven.execution.Message
import io.factdriven.execution.Type
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Node {

    val id: String
    val type: Type
    val entity: KClass<*>
    val label: String

    val parent: Node?
    val children: List<Node>
    val position: Int

    fun get(id: String): Node?
    fun <N: Node> get(id: String, type: KClass<in N> = Node::class): N?

    val first: Node
    val last: Node
    val previous: Node?
    val next: Node?

    fun isFirstChild(): Boolean
    fun isLastChild(): Boolean
    fun isParent(): Boolean
    fun isRoot(): Boolean
    fun isChild(): Boolean
    fun isFirstChildOfRoot(): Boolean
    fun isLastChildOfRoot(): Boolean

    fun <N: Node> find(nodeOfType: KClass<N>, dealingWith: KClass<*>? = null): N?
    fun <N: Node> filter(nodesOfType: KClass<N>, dealingWith: KClass<*>? = null): List<N>

    fun findReceptorsFor(message: Message): List<Receptor>

}
