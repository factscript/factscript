package io.factdriven.execution.camunda.model

import io.factdriven.definition.Branching
import io.factdriven.definition.Conditional
import io.factdriven.definition.Flow
import io.factdriven.definition.Gateway

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Branch(node: Branching, parent: Element<out Flow, *>): Group<Branching>(node, parent) {

    override val dimension: Dimension get() = Dimension(
        width = fork.dimension.width + children.maxBy { it.dimension.width }!!.dimension.width + join.dimension.width,
        height = sequences.sumBy { it.dimension.height }
    )

    var fork: GatewaySymbol<*>
    var join: GatewaySymbol<*>

    @Suppress("UNCHECKED_CAST")
    val sequences: List<Sequence> get() = children.filter { it is Sequence } as List<Sequence>

    override fun entry(from: Direction): Position {
        val children = listOf(fork, sequences.first(), join)
        return when(from) {
            Direction.North -> Position(dimension.width / 2, 0)
            Direction.East -> Position(dimension.width, children.maxBy { it.entry(Direction.East).y }?.entry(Direction.East)?.y ?: dimension.height / 2)
            Direction.South -> Position(dimension.width / 2, dimension.height)
            Direction.West -> Position(0, children.maxBy { it.entry(Direction.West).y }?.entry(Direction.West)?.y ?: dimension.height / 2)
        }
    }

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
        sequences.map {
            val conditional = it.node.children.first().let { if (it is Conditional) it else null }
            if (it.children.isNotEmpty()) Path(fork, it.children.first(), it, conditional) else Path(fork, join, it, conditional)
        } + sequences.mapNotNull {
            if (it.children.isNotEmpty()) Path(it.children.last(), join, it, if (it.children.last() is Loop) it.children.last().node.children.last() as Conditional else null) else null
        }

    override fun position(child: Element<*,*>): Position {
        val y = (listOf(fork, join) + sequences.first()).maxBy { it.entry(Direction.West).y }!!.entry(Direction.West) - fork.entry(Direction.West)
        return position + if (child == fork) y
            else if (child == join) Position(fork.dimension.width + sequences.maxBy { it.dimension.width }!!.dimension.width, 0) + y
            else Position(fork.dimension.width, sequences.subList(0, sequences.indexOf(child)).sumBy { it.dimension.height })
    }

}