package io.factdriven.implementation

import io.factdriven.definition.api.Node
import io.factdriven.definition.api.Throwing
import kotlin.reflect.KClass

open class ThrowingImpl(parent: Node): Throwing, NodeImpl(parent) {

    override lateinit var throwing: KClass<*>
    override lateinit var instance: Any.() -> Any

}