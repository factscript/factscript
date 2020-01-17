package io.factdriven.definition

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Catching: Child {

    val catchingType: KClass<*>
    val catchingProperties: List<String>
    val matchingValues: List<Any.() -> Any?>

}

open class CatchingImpl(override val parent: Node): Catching, ChildImpl(parent) {

    override lateinit var catchingType: KClass<*>
    override val catchingProperties = mutableListOf<String>()
    override val matchingValues = mutableListOf<Any.() -> Any?>()

}