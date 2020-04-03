package io.factdriven.definition.api

import io.factdriven.execution.type
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Node {

    val id: String
    val entity: KClass<*>
    val label: String

    val parent: Node?
    val children: List<Node>
    val index: Int

    val first: Node
    val last: Node
    val previous: Node?
    val next: Node?

    fun isFirst(): Boolean
    fun isLast(): Boolean

    fun findCatching(catching: KClass<*>): Catching?
    fun findConsuming(consuming: KClass<*>): Consuming?
    fun findThrowing(throwing: KClass<*>): Throwing?
    fun findExecuting(executing: KClass<*>): Executing?
    fun findPromising(promising: KClass<*> = Any::class): Promising?
    fun <N: Node> findById(id: String, type: KClass<in N> = Node::class): N?

}
