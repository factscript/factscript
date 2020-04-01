package io.factdriven.definition

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Catching : Node {

    val catchingType: KClass<*>

}

interface Consuming: Catching {

    val catchingProperties: List<String>
    val matchingValues: List<Any.() -> Any?>

}

interface Promising: Consuming {

    val successType: KClass<*>?

}

interface Executing: Throwing, Catching

open class ConsumingImpl(parent: Node): Consuming, NodeImpl(parent) {

    override lateinit var catchingType: KClass<*>
    override val catchingProperties = mutableListOf<String>()
    override val matchingValues = mutableListOf<Any.() -> Any?>()

}

open class PromisingImpl(parent: Node): Promising, ConsumingImpl(parent) {

    override var successType: KClass<*>? = null

}

open class ExecutingImpl(parent: Node): Executing, ThrowingImpl(parent) {

    override val catchingType: KClass<*>
        get() = Definition.getPromisingNodeByCatchingType(throwingType).successType!!

}