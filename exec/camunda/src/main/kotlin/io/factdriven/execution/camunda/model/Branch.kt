package io.factdriven.execution.camunda.model

import io.factdriven.definition.Branching
import io.factdriven.definition.Conditional
import io.factdriven.definition.Flow
import io.factdriven.definition.Gateway
import io.factdriven.execution.camunda.diagram.*
import io.factdriven.execution.camunda.model.BpmnModel.Companion.margin

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Branch(node: Branching, parent: Element<out Flow, *>): Group<Branching>(node, parent) {

    override val dimension: Dimension
        get() = Dimension(
            width = fork.dimension.width + children.dimensions.maxWidth + join.dimension.width,
            height = vertical.dimensions.sumHeight
        )

    var fork: GatewaySymbol<*>
    var join: GatewaySymbol<*>

    @Suppress("UNCHECKED_CAST")
    val vertical: List<Sequence> get() = children.filter { it is Sequence } as List<Sequence>
    val horizontal: List<Element<*,*>> get() = listOf(fork, vertical.first(), join)

    override fun entry(from: Direction): Position {
        return when(from) {
            Direction.North -> Position(
                dimension.width / 2,
                0
            ) south margin
            Direction.East -> Position(
                dimension.width,
                horizontal.maxBy { it.entry(from).y }?.entry(from)?.y ?: dimension.height / 2
            ) west margin
            Direction.South -> Position(
                dimension.width / 2,
                dimension.height
            ) north margin
            Direction.West -> Position(
                0,
                horizontal.maxBy { it.entry(from).y }?.entry(from)?.y ?: dimension.height / 2
            ) east margin
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
        vertical.map {
            val conditional = it.node.children.first().let { if (it is Conditional) it else null }
            if (it.children.isNotEmpty()) Path(fork, it.children.first(), it, conditional) else Path(fork, join, it, conditional)
        } + vertical.mapNotNull {
            if (it.children.isNotEmpty()) Path(it.children.last(), join, it, if (it.children.last() is Loop) it.children.last().node.children.last() as Conditional else null) else null
        }

    override fun position(child: Element<*,*>): Position {
        val y = horizontal.maxBy { it.entry().y }!!.entry() - fork.entry()
        return position + if (child == fork) y
            else if (child == join) Position(
            fork.dimension.width + vertical.dimensions.maxWidth,
            0
        ) + y
            else Position(
            fork.dimension.width,
            vertical.subList(0, vertical.indexOf(child)).sumBy { it.dimension.height })
    }

}