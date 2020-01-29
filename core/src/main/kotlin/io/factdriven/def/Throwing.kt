package io.factdriven.def

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Throwing: Child {

    val throwingType: KClass<*>
    val constructor: Any.() -> Any

}

open class ThrowingImpl(parent: Node): Throwing, ChildImpl(parent) {

    override lateinit var throwingType: KClass<*>
    override lateinit var constructor: Any.() -> Any

}