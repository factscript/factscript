package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.Branching
import io.factdriven.language.definition.Conditional
import io.factdriven.language.definition.Flow
import io.factdriven.language.definition.Gateway
import io.factdriven.language.visualization.bpmn.diagram.*

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
        var sequenceDiagram = vertical.first().diagram
        fork.diagram.westOf(sequenceDiagram)
        vertical.subList(1, vertical.size).forEach {
            if (it.node.children.isEmpty() || (it.node.children.first() is Conditional && (it.node.children.first() as Conditional).condition == null)) {
                it.diagram.northOf(vertical.first().diagram)
            } else {
                sequenceDiagram = sequenceDiagram.northOf(it.diagram) as Container
            }
        }
        join.diagram.eastOf(vertical.first().diagram)
    }

}