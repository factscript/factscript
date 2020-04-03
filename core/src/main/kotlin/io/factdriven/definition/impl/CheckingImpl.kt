package io.factdriven.definition.impl

import io.factdriven.definition.api.Checking
import io.factdriven.definition.api.Node

open class CheckingImpl(parent: Node): Checking, ExecutingImpl(parent) {

    override lateinit var condition: Any.() -> Boolean

}