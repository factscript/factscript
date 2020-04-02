package io.factdriven.definition.impl

import io.factdriven.definition.api.Branching
import io.factdriven.definition.api.Executing
import io.factdriven.definition.api.Gateway

open class BranchingImpl(parent: Executing): Branching, ExecutingImpl(parent) {

    override lateinit var gateway: Gateway

}