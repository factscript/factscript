package io.factdriven.definition

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Promising: Consuming {

    val successType: KClass<*>?

}

open class PromisingImpl(parent: Node): Promising, ConsumingImpl(parent) {

    override var successType: KClass<*>? = null

}