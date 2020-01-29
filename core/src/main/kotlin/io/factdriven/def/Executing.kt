package io.factdriven.def

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Executing: Throwing, Catching

open class ExecutingImpl(parent: Node): Executing, ThrowingImpl(parent) {

    override val catchingType: KClass<*>
        get() = Definition.getPromisingNodeByCatchingType(throwingType).successType

    override val catchingProperties = emptyList<String>()

    override val matchingValues = emptyList<Any.() -> Any?>()

}