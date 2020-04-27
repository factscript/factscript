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

    override val conditional: Conditional? get() = if (!hasJoin()) branches.find { it.elements.isEmpty() && !it.node.isFailing() }?.node?.find(Conditional::class) else null

    private fun hasJoin() = join != null || node.children.count { !it.asType<Flow>()!!.isFailing() } > 1

    var join: GatewaySymbol<*>? = if (hasJoin())
        when(node.gateway) {
            Gateway.Exclusive -> ExclusiveGatewaySymbol(node, this)
            Gateway.Inclusive -> InclusiveGatewaySymbol(node, this)
            Gateway.Parallel -> ParallelGatewaySymbol(node, this)
            Gateway.Catching -> ExclusiveGatewaySymbol(node, this)
        } else null

    override val elements: List<Element<*,*>> = listOf(fork) + node.children.map { Sequence(it as Flow, this) } + listOfNotNull(join)

    override val west: Symbol<*, *> get() = fork
    override val east: Symbol<*, *> get() = join ?: branches.find { !it.node.isFailing() }?.east ?: fork

    init {
        this.branches.mapNotNull { sequence ->
            if (sequence.elements.isNotEmpty())
                Path(fork, sequence, sequence, sequence.node.find(Conditional::class))
            else if (join != null)
                Path(fork, join!!, sequence, sequence.node.find(Conditional::class))
            else null
        } + this.branches.mapNotNull { sequence ->
            if (sequence.elements.isNotEmpty() && hasJoin() && !sequence.node.isFailing())
                Path(sequence, join!!, sequence, sequence.conditional) else null
        }
    }

    override fun initDiagram() {

        fork.diagram.westEntryOf(diagram)
        var sequenceDiagram = branches.first().diagram
        fork.diagram.westOf(sequenceDiagram)

        branches.subList(1, branches.size).forEach {
            if (it.node.firstChild?.asType<Conditional>()?.condition == null) {
                it.diagram.northOf(branches.first().diagram)
            } else {
                sequenceDiagram = sequenceDiagram.northOf(it.diagram) as Container
            }
        }

        val notFailingSequence = branches.find { it.elements.isNotEmpty() && !it.node.isFailing() }
        join?.diagram?.eastOf(branches.first().diagram)
            ?: (notFailingSequence?.diagram
                ?: fork.diagram).eastEntryOf(diagram)

    }

}