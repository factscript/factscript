package io.factdriven.implementation

import io.factdriven.definition.api.Checking
import io.factdriven.definition.api.Node

open class CheckingImpl(parent: Node): Checking, NodeImpl(parent) {

    override lateinit var condition: Any.() -> Boolean
    override lateinit var label: String protected set

}