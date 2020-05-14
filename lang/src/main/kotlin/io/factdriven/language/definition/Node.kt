package io.factdriven.language.definition

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
    val description: String

    val root: Flow
    val promise: Promising
    val parent: Node?
    val children: List<Node>
    val ancestors: List<Node>
    val descendants: List<Node>
    val position: Int

    fun get(id: String): Node?
    fun <N: Node> get(id: String, type: KClass<in N> = Node::class): N?

    fun isRoot(): Boolean
    fun isParent(): Boolean
    fun isChild(): Boolean

    val firstSibling: Node
    val lastSibling: Node
    val previousSibling: Node?
    val nextSibling: Node?

    fun isFirstSibling(): Boolean
    fun isLastSibling(): Boolean

    val start: Node
    val finish: Node
    val forward: Node?
    val backward: Node?

    val firstChild: Node?
    val lastChild: Node?

    fun isStart(): Boolean
    fun isFinish(): Boolean

    fun <N: Node> find(nodeOfType: KClass<N>, dealingWith: KClass<*>? = null): N?
    fun <N: Node> filter(nodesOfType: KClass<N>, dealingWith: KClass<*>? = null): List<N>

    fun findReceptorsFor(message: Message): Set<Receptor>

}
