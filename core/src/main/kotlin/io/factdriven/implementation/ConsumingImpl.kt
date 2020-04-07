package io.factdriven.implementation

import io.factdriven.definition.api.Consuming
import io.factdriven.definition.api.Node
import kotlin.reflect.KClass

open class ConsumingImpl(parent: Node): Consuming, NodeImpl(parent) {

    override lateinit var catching: KClass<*>
    override val properties = mutableListOf<String>()
    override val matching = mutableListOf<Any.() -> Any?>()

}