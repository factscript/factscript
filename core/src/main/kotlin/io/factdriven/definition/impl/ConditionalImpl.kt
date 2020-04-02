package io.factdriven.definition.impl

import io.factdriven.definition.api.Conditional
import io.factdriven.definition.api.Node

open class ConditionalImpl(parent: Node): Conditional, NodeImpl(parent) {

    override lateinit var condition: Any.() -> Boolean

}