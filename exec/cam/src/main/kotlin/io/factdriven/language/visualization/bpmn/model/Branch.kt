package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.language.impl.utils.asType
import io.factdriven.language.visualization.bpmn.diagram.Container

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Branch(node: Branching, parent: Element<out Flow, *>): Group<Branching>(node, parent) {

    var fork: GatewaySymbol<*> = map(node.fork)!!

    val branches: List<Sequence> get() = elements.filterIsInstance<Sequence>()

    override val conditional: Conditional? get() {
        return if (!hasJoin()) branches.find { it.node.isContinuing() }?.conditional else null
    }

    override val exit: Group<*> get() = branches.find { sequence -> sequence.node.isContinuing() }!!.let { if (hasJoin() && ((it.node as? OptionalFlow)?.isDefault() == true)) branches.first() else it }

    private fun hasJoin() = join != null || node.join != null

    var join: GatewaySymbol<*>? = map(node.join)

    private fun map(junction: Junction?): GatewaySymbol<*>? = when(junction) {
        Junction.One -> ExclusiveGatewaySymbol(node, this)
        Junction.Some -> InclusiveGatewaySymbol(node, this)
        Junction.All -> ParallelGatewaySymbol(node, this)
        Junction.First -> EventBasedGatewaySymbol(node, this)
        else -> null
    }

    override val elements: List<Element<*,*>> = listOf(fork) + node.children.map { Sequence(it as Flow, this) } + listOfNotNull(join)

    override val west: Symbol<*, *> get() = fork
    override val east: Symbol<*, *> get() = join ?: exit.east

    init {
        this.branches.mapNotNull { sequence ->
            if (sequence.elements.isNotEmpty())
                Path(fork, sequence, sequence, sequence.node.find(Conditional::class))
            else if (join != null)
                Path(fork, join!!, sequence, sequence.node.find(Conditional::class))
            else null
        } + this.branches.mapNotNull { sequence ->
            if (sequence.elements.isNotEmpty() && hasJoin() && sequence.node.isContinuing())
                Path(sequence, join!!, sequence, sequence.conditional) else null
        }
    }

    override fun initDiagram() {

        fork.diagram.westEntryOf(diagram)
        var sequenceDiagram = branches.first().diagram
        fork.diagram.westOf(sequenceDiagram)

        branches.subList(1, branches.size).forEach {
            if ((it.node as? OptionalFlow)?.isDefault() == true) {
                it.diagram.northOf(branches.first().diagram)
            } else {
                sequenceDiagram = sequenceDiagram.northOf(it.diagram) as Container
            }
        }
        join?.diagram?.eastOf(exit.diagram)
            ?: exit.diagram.eastEntryOf(diagram)

    }

}