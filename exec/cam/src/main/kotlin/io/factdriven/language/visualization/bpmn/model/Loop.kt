package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.language.impl.definition.OptionalFlowImpl
import io.factdriven.language.visualization.bpmn.diagram.*
import io.factdriven.language.impl.definition.ConditionalImpl

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Loop(node: RepeatingFlow, parent: Element<*,*>): Group<RepeatingFlow>(node, parent), Looping {

    override fun isSucceeding() = node.isSucceeding()
    override fun isFailing() = node.isFailing()

    override val condition: Any.() -> Boolean get() = node.condition

    private var join: GatewaySymbol<*>
    private var forward: Sequence
    private var backward: Sequence
    internal var fork: GatewaySymbol<*>

    override val west: Symbol<*, *> get() = join
    override val east: Symbol<*, *> get() = fork

    override val diagram: Container = Container(36)
    override val elements: List<Element<*, *>>

    override val exitConditional: ConditionalNode? get() = node.conditional

    init {

        join = ExclusiveGatewaySymbol(node.conditional, this)
        forward = Sequence(node, this)
        backward = let {
            val execution = OptionalFlowImpl(node.entity, node)
            execution.children.add(ConditionalImpl<Any>(execution)("No") as Node)
            Sequence(execution, this)
        }
        fork = ExclusiveGatewaySymbol(node.conditional, this)

        elements = listOf(join) + forward + backward + fork

        Path(join, forward, forward, null)
        Path(forward, fork, forward, forward.exitConditional)
        Path(fork, join, backward, (backward.node as ConditionalFlow).conditional)

    }

    override fun initDiagram() {
        join.diagram.westEntryOf(diagram)
        forward.diagram.eastOf(join.diagram)
        backward.diagram.northOf(forward.diagram)
        fork.diagram.eastOf(forward.diagram)
    }

}