package io.factdriven.execution.camunda.model

import io.factdriven.definition.*
import io.factdriven.execution.camunda.diagram.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Sequence(node: Flow, parent: Element<*,*>): Group<Flow>(node,parent) {

    override val children: List<Element<*,*>> = node.children.mapNotNull {
        when (it) {
            is Calling -> ServiceTaskSymbol(it, this)
            is Promising -> CatchingEventSymbol(it, this)
            is Awaiting -> ReceiveTaskSymbol(it, this)
            is Throwing -> if (it.isFinish()) ThrowingEventSymbol(it, this) else SendTaskSymbol(it, this)
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
            Path(previous, it, this, if (previous is Loop) previous.node.children.last() as Conditional else null)
        } else emptyList()

    override fun initDiagram() {
        if (children.isNotEmpty())
            (children.first().diagram as Box).insideOf(diagram)
        if (children.size > 1) children.subList(1, children.size).forEachIndexed { index, element ->
            (element.diagram as Box).eastOf(children[index].diagram as Box)
        }
    }

}