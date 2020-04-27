package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.language.impl.utils.asType
import io.factdriven.language.visualization.bpmn.diagram.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Branch(node: Branching, parent: Element<out Flow, *>): Group<Branching>(node, parent) {

    var fork: GatewaySymbol<*> = when(node.gateway) {
        Gateway.Exclusive -> ExclusiveGatewaySymbol(node, this)
        Gateway.Inclusive -> InclusiveGatewaySymbol(node, this)
        Gateway.Parallel -> ParallelGatewaySymbol(node, this)
        Gateway.Catching -> EventBasedGatewaySymbol(node, this)
    }

    val branches: List<Sequence> get() = elements.filterIsInstance<Sequence>()

    override val conditional: Conditional? get() = null

    var join: GatewaySymbol<*>? = if (node.children.count { it.children.isEmpty() || it.find(Throwing::class)?.isFailing() != true } > 1)
        when(node.gateway) {
            Gateway.Exclusive -> ExclusiveGatewaySymbol(node, this)
            Gateway.Inclusive -> InclusiveGatewaySymbol(node, this)
            Gateway.Parallel -> ParallelGatewaySymbol(node, this)
            Gateway.Catching -> ExclusiveGatewaySymbol(node, this)
        } else null

    override val elements: List<Element<*,*>> = listOf(fork) + node.children.map { Sequence(it as Flow, this) } + listOfNotNull(join)

    override val west: Symbol<*, *> get() = fork
    override val east: Symbol<*, *> get() = if (join != null) join!! else branches.find { it.elements.isNotEmpty() && it.elements.last().node.asType<Throwing>()?.isFailing() != true }?.east ?: fork

    init {
        this.branches.mapNotNull { sequence ->
            if (sequence.elements.isNotEmpty()) Path(fork, sequence, sequence, sequence.node.find(Conditional::class)) else if (join != null) Path(fork, join!!, sequence, sequence.node.find(Conditional::class)) else null
        } + this.branches.mapNotNull { sequence ->
            if (sequence.elements.isNotEmpty() && join != null && sequence.node.find(Throwing::class)?.isFailing() != true) Path(sequence, join!!, sequence, sequence.conditional) else null
        }
    }

    override fun initDiagram() {
        fork.diagram.westEntryOf(diagram)
        var sequenceDiagram = branches.first().diagram
        fork.diagram.westOf(sequenceDiagram)
        branches.subList(1, branches.size).forEach {
            if (it.node.children.isEmpty() || (it.node.children.first() is Conditional && (it.node.children.first() as Conditional).condition == null)) {
                it.diagram.northOf(branches.first().diagram)
            } else {
                sequenceDiagram = sequenceDiagram.northOf(it.diagram) as Container
            }
        }
        if (join != null)
            join!!.diagram.eastOf(branches.first().diagram)
        else {
            val sequence = branches.find { it.elements.isNotEmpty() && it.elements.last().node.asType<Throwing>()?.isFailing() != true }
            if (sequence != null)  {
                sequence.diagram.eastEntryOf(diagram)
            } else {
                fork.diagram.eastEntryOf(diagram)
            }
        }
    }

}