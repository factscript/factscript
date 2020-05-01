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

    internal val sequences get() = elements.filterIsInstance<Sequence>()

    override val conditional: Conditional? get() = null

    init {

        task = when (node) {
            is Calling -> ServiceTaskSymbol(node, this)
            is ConsumingEvent -> ReceiveTaskSymbol(node, this)
            is Throwing -> SendTaskSymbol(node, this)
            else -> throw IllegalStateException()
        }

        elements = listOf(task) + node.children.map { Sequence(it as Flow, this) }

    }

    override fun initDiagram() {
        task.diagram.westEntryOf(diagram)
        assert(sequences.size <= 3) { "More than three boundary events are not supported for tasks." }
        sequences.getOrNull(0)?.diagram?.southOf(task.diagram)
        sequences.getOrNull(1)?.diagram?.southOf(sequences[0].diagram)
        sequences.getOrNull(2)?.diagram?.northOf(task.diagram)
        // sequences.getOrNull(3)?.diagram?.northOf(sequences[2].diagram) TODO()
        task.diagram.eastEntryOf(diagram)
    }

}