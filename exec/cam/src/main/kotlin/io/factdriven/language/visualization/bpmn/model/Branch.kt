package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.language.visualization.bpmn.diagram.Container

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Branch(node: Branching, parent: Element<out Flow, *>): Group<Branching>(node, parent) {

    override fun isSucceeding() = node.isSucceeding()
    override fun isFailing() = node.isFailing()

    var fork: GatewaySymbol<*> = map(node.fork)!!
    var join: GatewaySymbol<*>? = map(node.join)

    val sequences: List<Sequence> get() = elements.filterIsInstance<Sequence>()

    override val exitConditional: ConditionalNode? get() {
        return if (join == null) sequences.find { it.isContinuing() }?.exitConditional else null
    }

    override val exitGroup: Group<*> get() = sequences.find { sequence -> sequence.isContinuing() }!!.let {
        if (join != null && ((it.node as? OptionalFlow)?.isDefault() == true)) sequences.first() else it
    }

    private fun map(junction: Junction?): GatewaySymbol<*>? = when(junction) {
        Junction.One -> ExclusiveGatewaySymbol(node, this)
        Junction.Some -> InclusiveGatewaySymbol(node, this)
        Junction.All -> ParallelGatewaySymbol(node, this)
        Junction.First -> EventBasedGatewaySymbol(node, this)
        else -> null
    }

    override val elements: List<Element<*,*>> = listOf(fork) + node.children.map { Sequence(it as Flow, this) } + listOfNotNull(join)

    override val west: Symbol<*, *> get() = fork
    override val east: Symbol<*, *> get() = join ?: exitGroup.east

    init {
        this.sequences.mapNotNull { sequence ->
            if (sequence.elements.isNotEmpty())
                Path(fork, sequence, sequence, sequence.node.find(ConditionalNode::class))
            else if (join != null)
                Path(fork, join!!, sequence, sequence.node.find(ConditionalNode::class))
            else null
        } + this.sequences.mapNotNull { sequence ->
            if (sequence.elements.isNotEmpty() && join != null && sequence.isContinuing())
                Path(sequence, join!!, sequence, sequence.exitConditional) else null
        }
    }

    override fun initDiagram() {

        fork.diagram.westEntryOf(diagram)
        var sequenceDiagram = sequences.first().diagram
        fork.diagram.westOf(sequenceDiagram)

        sequences.subList(1, sequences.size).forEach {
            if ((it.node as? OptionalFlow)?.isDefault() == true) {
                it.diagram.northOf(sequences.first().diagram)
            } else {
                sequenceDiagram = sequenceDiagram.northOf(it.diagram) as Container
            }
        }
        join?.diagram?.eastOf(exitGroup.diagram)
            ?: exitGroup.diagram.eastEntryOf(diagram)

    }

}