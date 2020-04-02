package io.factdriven.definition.impl

import io.factdriven.definition.api.Branching
import io.factdriven.definition.api.Node
import io.factdriven.definition.api.Gateway

open class BranchingImpl(parent: Node): Branching, NodeImpl(parent) {

    override lateinit var gateway: Gateway

}