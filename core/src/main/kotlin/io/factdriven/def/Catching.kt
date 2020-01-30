package io.factdriven.def

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Catching : Child {

    val catchingType: KClass<*>

}

interface Consuming: Catching {

    val catchingProperties: List<String>
    val matchingValues: List<Any.() -> Any?>

}

open class ConsumingImpl(parent: Node): Consuming, ChildImpl(parent) {

    override lateinit var catchingType: KClass<*>
    override val catchingProperties = mutableListOf<String>()
    override val matchingValues = mutableListOf<Any.() -> Any?>()

}

interface Executing: Throwing, Catching

open class ExecutingImpl(parent: Node): Executing, ThrowingImpl(parent) {

    override val catchingType: KClass<*>
        get() = Definition.getPromisingNodeByCatchingType(throwingType).successType

}