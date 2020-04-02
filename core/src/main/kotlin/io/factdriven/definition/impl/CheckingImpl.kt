package io.factdriven.definition.impl

import io.factdriven.definition.api.Checking
import io.factdriven.definition.api.Executing

open class CheckingImpl(parent: Executing): Checking, ExecutingImpl(parent) {

    override lateinit var condition: Any.() -> Boolean

}