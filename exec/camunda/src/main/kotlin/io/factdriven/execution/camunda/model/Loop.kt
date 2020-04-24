package io.factdriven.execution.camunda.model

import io.factdriven.definition.*
import io.factdriven.execution.camunda.diagram.*
import io.factdriven.impl.definition.ConditionalImpl

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Loop(node: Looping, parent: Element<*,*>): Group<Flow>(node,parent) {

    var join: GatewaySymbol<*>
    var sequence: Sequence
    var fork: GatewaySymbol<*>

    override val diagram: Container = Container(36)
    private val loop: Container = Container(36)

    override val children: List<Element<*, *>> = let {
        join = ExclusiveGatewaySymbol(node.children.last(), this)
        sequence = Sequence(node, this)
        fork = ExclusiveGatewaySymbol(node.children.last(), this)
        listOf(join) + sequence + fork
    }

    override val paths: List<Path> = listOf(
        Path(join, sequence, this),
        Path(sequence, fork, this, if (sequence.children.last() is Loop) sequence.children.last().node.children.last() as Conditional else null),
        Path(fork, join, this, ConditionalImpl<Any>(node), loop)
    )

    override fun initDiagram() {
        join.diagram.insideOf(diagram)
        sequence.diagram.eastOf(join.diagram)
        loop.northOf(sequence.diagram)
        fork.diagram.eastOf(sequence.diagram)
    }

}