package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.language.visualization.bpmn.diagram.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Sequence(node: Flow, parent: Element<*,*>): Group<Flow>(node,parent) {

    override val children: List<Element<*,*>> = node.children.mapNotNull {
        when (it) {
            is Calling -> ServiceTaskSymbol(it, this)
            is Promising -> CatchingEventSymbol(it, this)
            is Awaiting -> ReceiveTaskSymbol(it, this)
            is Throwing -> if (it.isFinish() || it.isFailing()) ThrowingEventSymbol(it, this) else SendTaskSymbol(it, this)
            is Branching -> Branch(it, this)
            is Looping -> Loop(it, this)
            is Flow -> Sequence(it, this)
            is Conditional -> null
            else -> throw IllegalStateException()
        }
    }

    override val paths: List<Path> =
        if (children.size > 1) children.subList(1, children.size).map {
            val previous = children.get(children.indexOf(it) - 1)
            val conditional = let {
                if (previous is Loop) previous.node.children.last() as Conditional
                else if (previous is Branch && previous.join == null && previous.east is GatewaySymbol)
                    previous.vertical.find { it.children.isEmpty() }!!.node.children.first() as Conditional else null
            }
            val via = let {
                if (previous is Branch && previous.join == null && previous.east is GatewaySymbol)
                    previous.vertical.find { it.children.isEmpty() }!!.diagram else null
            }
            Path(previous, it, it, conditional, via)
        } else emptyList()

    override val west: Symbol<*, *> get() = children.first().west
    override val east: Symbol<*, *> get() = children.last().east

    override fun initDiagram() {
        if (children.isNotEmpty())
            (children.first().diagram as Box).westEntryOf(diagram)
        if (children.size > 1) children.subList(1, children.size).forEachIndexed { index, element ->
            (element.diagram as Box).eastOf(children[index].diagram as Box)
        }
    }

}