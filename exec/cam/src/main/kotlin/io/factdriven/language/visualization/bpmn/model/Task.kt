package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.language.visualization.bpmn.diagram.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Task(node: Node, parent: Element<*,*>): Group<Node>(node, parent) {

    internal var task: TaskSymbol<out Node, out org.camunda.bpm.model.bpmn.instance.Task>

    override val west: Symbol<*, *> get() = task
    override val east: Symbol<*, *> get() = task

    override val diagram: Container = Container(36)
    override val elements: List<Element<*, *>>

    private val sequences get() = elements.filterIsInstance<Sequence>()

    override val conditional: Conditional? get() = null

    init {

        task = when (node) {
            is Calling -> ServiceTaskSymbol(node, this)
            is Awaiting -> ReceiveTaskSymbol(node, this)
            is Throwing -> SendTaskSymbol(node, this)
            else -> throw IllegalStateException()
        }

        elements = listOf(task) + node.children.map { Sequence(it as Flow, this) }

    }

    override fun initDiagram() {
        task.diagram.westEntryOf(diagram)
        sequences.forEach {
            it.diagram.southOf(task.diagram)
        }
        task.diagram.eastEntryOf(diagram)
    }

}