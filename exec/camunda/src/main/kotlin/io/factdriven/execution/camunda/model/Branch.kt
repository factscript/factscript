package io.factdriven.execution.camunda.model

import io.factdriven.definition.Branching
import io.factdriven.definition.Conditional
import io.factdriven.definition.Flow
import io.factdriven.definition.Gateway
import io.factdriven.execution.camunda.diagram.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Branch(node: Branching, parent: Element<out Flow, *>): Group<Branching>(node, parent) {

    var fork: GatewaySymbol<*>
    var join: GatewaySymbol<*>

    @Suppress("UNCHECKED_CAST")
    val vertical: List<Sequence> get() = children.filter { it is Sequence } as List<Sequence>
    val horizontal: List<Element<*,*>> get() = listOf(fork, vertical.first(), join)

    override val children: List<Element<*,*>> = let {
        fork = when(node.gateway) {
            Gateway.Exclusive -> ExclusiveGatewaySymbol(node, this)
            Gateway.Inclusive -> InclusiveGatewaySymbol(node, this)
            Gateway.Parallel -> ParallelGatewaySymbol(node, this)
            Gateway.Catching -> EventBasedGatewaySymbol(node, this)
        }
        join = when(node.gateway) {
            Gateway.Exclusive -> ExclusiveGatewaySymbol(node, this)
            Gateway.Inclusive -> InclusiveGatewaySymbol(node, this)
            Gateway.Parallel -> ParallelGatewaySymbol(node, this)
            Gateway.Catching -> ExclusiveGatewaySymbol(node, this)
        }
        val sequences = node.children.map { Sequence(it as Flow, this) }
        listOf(fork) + sequences + listOf(join)
    }

    override val paths: List<Path> =
        vertical.map { sequence ->
            val conditional = sequence.node.children.first().let { if (it is Conditional) it else null }
            if (sequence.children.isNotEmpty()) Path(fork, sequence, sequence, conditional) else Path(fork, join, sequence, conditional)
        } + vertical.mapNotNull { sequence ->
            if (sequence.children.isNotEmpty()) Path(sequence, join, sequence, if (sequence.children.last() is Loop) sequence.children.last().node.children.last() as Conditional else null) else null
        }

    override fun initDiagram() {
        fork.diagram.insideOf(diagram)
        fork.diagram.westOf(vertical.first().diagram)
        vertical.subList(1, vertical.size).forEachIndexed { index, element ->
            (element.diagram as Box).southOf(vertical[index].diagram as Box)
        }
        join.diagram.eastOf(vertical.first().diagram)
    }

}