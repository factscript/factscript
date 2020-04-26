package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.language.impl.definition.ConditionalImpl
import io.factdriven.language.impl.utils.asType
import io.factdriven.language.visualization.bpmn.diagram.*
import io.factdriven.language.visualization.bpmn.model.Element.Companion.asType

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
            is Conditional -> null // Label(it, this)
            else -> throw IllegalStateException()
        }
    }

    val paths2: List<Path> = if (children.size > 1)
        children.subList(1, children.size).mapIndexed { i, it ->
            Path(children[i], it, it.asType<Group<*>>() ?: children[i].asType<Group<*>>() ?: this)
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