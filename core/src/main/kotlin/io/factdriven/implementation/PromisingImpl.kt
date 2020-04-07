package io.factdriven.implementation

import io.factdriven.definition.api.Node
import io.factdriven.definition.api.Promising
import kotlin.reflect.KClass

open class PromisingImpl(parent: Node): Promising, ConsumingImpl(parent) {

    override var succeeding: KClass<*>? = null

}