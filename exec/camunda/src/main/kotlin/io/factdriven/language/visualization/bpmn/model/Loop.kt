package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.language.impl.definition.ConditionalExecutionImpl
import io.factdriven.language.visualization.bpmn.diagram.*
import io.factdriven.language.impl.definition.ConditionalImpl
import io.factdriven.language.impl.definition.TriggeredExecutionImpl

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Loop(node: Looping, parent: Element<*,*>): Group<Flow>(node,parent) {

    var join: GatewaySymbol<*>
    var forward: Sequence
    var backward: Sequence
    var fork: GatewaySymbol<*>

    override val diagram: Container = Container(36)

    override val children: List<Element<*, *>>

    init {

        join = ExclusiveGatewaySymbol(node.children.last(), this)
        forward = Sequence(node, this)
        backward = let {
            val execution = ConditionalExecutionImpl(node.entity, node)
            execution.children.add(ConditionalImpl<Any>(execution)("No") as Node)
            Sequence(execution, this)
        }
        fork = ExclusiveGatewaySymbol(node.children.last(), this)

        children = listOf(join) + forward + backward + fork

        Path(join, forward, forward)
        Path(forward, fork, forward)
        Path(fork, join, backward)

    }

    override val west: Symbol<*, *> get() = join
    override val east: Symbol<*, *> get() = fork

    override fun initDiagram() {
        join.diagram.westEntryOf(diagram)
        forward.diagram.eastOf(join.diagram)
        backward.diagram.northOf(forward.diagram)
        fork.diagram.eastOf(forward.diagram)
    }

}