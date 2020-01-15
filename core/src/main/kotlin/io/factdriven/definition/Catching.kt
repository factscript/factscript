package io.factdriven.definition

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Catching: Child {

    val catchingType: KClass<*>

}

open class CatchingImpl<T: Any>(override val parent: Node): Catching, ChildImpl(parent) {

    override lateinit var catchingType: KClass<*>

}