package io.factdriven.definition

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Throwing: Node {

    val throwingType: KClass<*>
    val constructor: Any.() -> Any

}

open class ThrowingImpl(parent: Node): Throwing, NodeImpl(parent) {

    override lateinit var throwingType: KClass<*>
    override lateinit var constructor: Any.() -> Any

}