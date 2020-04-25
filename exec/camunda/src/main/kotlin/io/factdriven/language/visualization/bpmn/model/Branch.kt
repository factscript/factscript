package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.language.visualization.bpmn.diagram.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Branch(node: Branching, parent: Element<out Flow, *>): Group<Branching>(node, parent) {

    var fork: GatewaySymbol<*>
    var join: GatewaySymbol<*>?

    @Suppress("UNCHECKED_CAST")
    val vertical: List<Sequence> get() = children.filter { it is Sequence } as List<Sequence>
    val horizontal: List<Element<*,*>> get() = listOf(fork, vertical.first(), join).filterNotNull()

    override val children: List<Element<*,*>> = let {
        fork = when(node.gateway) {
            Gateway.Exclusive -> ExclusiveGatewaySymbol(node, this)
            Gateway.Inclusive -> InclusiveGatewaySymbol(node, this)
            Gateway.Parallel -> ParallelGatewaySymbol(node, this)
            Gateway.Catching -> EventBasedGatewaySymbol(node, this)
        }
        val joinRequired = node.children.count { it.children.isEmpty() || it.children.last().asThrowing()?.isFailing() != true } > 1
        join = if (joinRequired) when(node.gateway) {
            Gateway.Exclusive -> ExclusiveGatewaySymbol(node, this)
            Gateway.Inclusive -> InclusiveGatewaySymbol(node, this)
            Gateway.Parallel -> ParallelGatewaySymbol(node, this)
            Gateway.Catching -> ExclusiveGatewaySymbol(node, this)
        } else null
        val sequences = node.children.map { Sequence(it as Flow, this) }
        listOf(fork) + sequences + listOf(join).filterNotNull()
    }

    override val paths: List<Path> =
        vertical.mapNotNull { sequence ->
            val conditional = sequence.node.children.first().let { if (it is Conditional) it else null }
            if (sequence.children.isNotEmpty()) Path(fork, sequence, sequence, conditional) else if (join != null) Path(fork, join!!, sequence, conditional) else null
        } + vertical.mapNotNull { sequence ->
            val node = sequence.node.children.last().asThrowing()?.isFailing() == true; node
            if (sequence.children.isNotEmpty() && join != null && sequence.node.children.last().asThrowing()?.isFailing() != true) Path(sequence, join!!, sequence, if (sequence.children.last() is Loop) sequence.children.last().node.children.last() as Conditional else null) else null
        }

    override val west: Symbol<*, *> get() = fork
    override val east: Symbol<*, *> get() = if (join != null) join!! else vertical.find { it.children.isNotEmpty() && it.children.last().node.asThrowing()?.isFailing() != true }?.east ?: fork

    override fun initDiagram() {
        fork.diagram.westEntryOf(diagram)
        var sequenceDiagram = vertical.first().diagram
        fork.diagram.westOf(sequenceDiagram)
        vertical.subList(1, vertical.size).forEach {
            if (it.node.children.isEmpty() || (it.node.children.first() is Conditional && (it.node.children.first() as Conditional).condition == null)) {
                it.diagram.northOf(vertical.first().diagram)
            } else {
                sequenceDiagram = sequenceDiagram.northOf(it.diagram) as Container
            }
        }
        if (join != null)
            join!!.diagram.eastOf(vertical.first().diagram)
        else {
            val sequence = vertical.find { it.children.isNotEmpty() && it.children.last().node.asThrowing()?.isFailing() != true }
            if (sequence != null)  {
                sequence.diagram.eastEntryOf(diagram)
            } else {
                fork.diagram.eastEntryOf(diagram)
            }
        }
    }

}