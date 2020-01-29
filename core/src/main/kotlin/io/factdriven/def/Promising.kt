package io.factdriven.def

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Promising: Catching {

    val successType: KClass<*>

}

open class PromisingImpl(parent: Node): Promising, CatchingImpl(parent) {

    override lateinit var successType: KClass<*>

}