package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.language.impl.utils.asType
import io.factdriven.language.visualization.bpmn.diagram.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Sequence(node: Flow, parent: Element<*,*>): Group<Flow>(node,parent) {

    override val west: Symbol<*, *> get() = elements.first().west
    override val east: Symbol<*, *> get() = elements.lastOrNull()?.east ?: parent!!.asType<Branch>()!!.fork

    override val conditional: Conditional? get() = elements.lastOrNull()?.asType<Loop>()?.conditional

    override val elements: List<Element<*,*>> = node.children.mapNotNull {
        when (it) {
            is Promising -> CatchingEventSymbol(it, this)
            is Catching -> Task(it, this)
            is Throwing -> if (it.isFinish() || !it.isContinuing()) ThrowingEventSymbol(it, this) else Task(it, this)
            is Branching -> Branch(it, this)
            is Looping -> Loop(it, this)
            is Flow -> Sequence(it, this)
            is Conditional -> null
            else -> throw IllegalStateException()
        }
    }

    init {
        if (elements.size > 1)
            elements.subList(1, elements.size).mapIndexed { i, it ->
                Path(
                    elements[i],
                    it,
                    elements[i].asType<Group<*>>()
                        ?: it.asType<Group<*>>()
                        ?: this,
                    elements[i].asType<Group<*>>()?.conditional
                )
            } else emptyList()
    }

    override fun initDiagram() {
        if (elements.isNotEmpty())
            (elements.first().diagram as Box).westEntryOf(diagram)
        if (elements.size > 1) elements.subList(1, elements.size).forEachIndexed { index, element ->
            (element.diagram as Box).eastOf(elements[index].diagram as Box)
        }
    }

}