package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.language.impl.utils.asType
import io.factdriven.language.visualization.bpmn.diagram.Box

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Sequence(node: Flow, parent: Element<*,*>): Group<Flow>(node,parent), Optional {

    override fun isSucceeding() = node.isSucceeding()
    override fun isFailing() = node.isFailing()

    override val condition: (Any.() -> Boolean)? get() = (node as? OptionalFlow)?.condition

    override val west: Symbol<*, *> get() = elements.first().west
    override val east: Symbol<*, *> get() = elements.lastOrNull()?.east ?: parent!!.asType<Branch>()!!.fork

    override val exitConditional: ConditionalNode? get() = elements.lastOrNull()?.asType<Group<*>>()?.exitConditional ?: if (elements.isEmpty()) node.children.firstOrNull()?.asType<ConditionalNode>() else null

    override val elements: List<Element<*,*>> = node.children.mapNotNull { child ->
        when (child) {
            is Executing -> listOf(Task(child, this))
            is Throwing -> if (child.factType == FactType.Event || child.isFinish() || !child.isContinuing()) {
                if (node.isFailing() && node.root.descendants.any { (it as? CorrelatingFlow)?.isCompensating() == true && (it as? CorrelatingFlow)?.consuming == child.throwing }) {
                    listOf(ThrowingEventSymbol(child, this, true), ThrowingEventSymbol(child, this))
                } else {
                    listOf(ThrowingEventSymbol(child, this))
                }
            } else {
                listOf(Task(child, this))
            }
            is Promising -> listOf(CatchingEventSymbol(child, this))
            is Consuming -> listOf(if (parent is Task && node.children.indexOf(child) == 0) BoundaryEventSymbol(child, this) else if ((parent is Branch && parent.node.fork == Junction.First) && node.children.indexOf(child) == 0) CatchingEventSymbol(child, this) else Task(child, this))
            is Catching -> listOf(if (parent is Task && node.children.indexOf(child) == 0) BoundaryEventSymbol(child, this) else CatchingEventSymbol(child, this))
            is Branching -> listOf(Branch(child, this))
            is LoopingFlow -> listOf(Loop(child, this))
            is Flow -> listOf(Sequence(child, this))
            is Conditional -> null
            else -> throw IllegalStateException()
        }
    }.flatten()

    init {
        if (elements.size > 1)
            elements.subList(1, elements.size).mapIndexed { i, it ->
                if ((node as? CorrelatingFlow)?.isCompensating() == true) {
                    Association(elements[i], it, elements[i].asType<Group<*>>() ?: it.asType<Group<*>>() ?: this)
                } else {
                    Path(elements[i], it, elements[i].asType<Group<*>>() ?: it.asType<Group<*>>() ?: this, elements[i].asType<Group<*>>()?.exitConditional)
                }
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