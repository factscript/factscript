package io.factdriven.definition.impl

import io.factdriven.definition.api.Consuming
import io.factdriven.definition.api.Executing
import kotlin.reflect.KClass

open class ConsumingImpl(parent: Executing): Consuming, ExecutingImpl(parent) {

    override lateinit var catching: KClass<*>
    override val properties = mutableListOf<String>()
    override val matching = mutableListOf<Any.() -> Any?>()

}