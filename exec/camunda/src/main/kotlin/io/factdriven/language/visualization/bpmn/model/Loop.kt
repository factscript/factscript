package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.language.impl.definition.ConditionalExecutionImpl
import io.factdriven.language.visualization.bpmn.diagram.*
import io.factdriven.language.impl.definition.ConditionalImpl
import io.factdriven.language.impl.definition.NodeImpl
import io.factdriven.language.impl.utils.asType

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Loop(node: Looping, parent: Element<*,*>): Group<Flow>(node,parent) {

    private var join: GatewaySymbol<*>
    private var forward: Sequence
    private var backward: Sequence
    internal var fork: GatewaySymbol<*>

    override val west: Symbol<*, *> get() = join
    override val east: Symbol<*, *> get() = fork

    override val diagram: Container = Container(36)
    override val elements: List<Element<*, *>>

    override val conditional: Conditional? get() = forward.node.find(Conditional::class)

    init {

        join = ExclusiveGatewaySymbol(node.children.last(), this)
        forward = Sequence(node, this)
        backward = let {
            val execution = ConditionalExecutionImpl(node.entity, node)
            execution.children.add(ConditionalImpl<Any>(execution)("No") as Node)
            Sequence(execution, this)
        }
        fork = ExclusiveGatewaySymbol(node.children.last(), this)

        elements = listOf(join) + forward + backward + fork

        Path(join, forward, forward, null)
        Path(forward, fork, forward, forward.conditional)
        Path(fork, join, backward, backward.node.find(Conditional::class))

    }

    override fun initDiagram() {
        join.diagram.westEntryOf(diagram)
        forward.diagram.eastOf(join.diagram)
        backward.diagram.northOf(forward.diagram)
        fork.diagram.eastOf(forward.diagram)
    }

}