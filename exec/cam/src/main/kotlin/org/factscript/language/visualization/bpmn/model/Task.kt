package org.factscript.language.visualization.bpmn.model

import org.factscript.language.definition.*
import org.factscript.language.impl.utils.asType
import org.factscript.language.visualization.bpmn.diagram.Container

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Task(node: Node, parent: Element<*,*>): Group<Node>(node, parent) {

    override fun isSucceeding() = (node as? Throwing)?.isSucceeding() ?: false
    override fun isFailing() = (node as? Throwing)?.isFailing() ?: false

    internal val task: TaskSymbol<out Node, out org.camunda.bpm.model.bpmn.instance.Task>

    override val west: Symbol<*, *> get() = task
    override val east: Symbol<*, *> get() = join ?: task

    override val diagram: Container = Container(36)
    override val elements: List<Element<*, *>>

    internal val sequences get() = elements.filterIsInstance<Sequence>()

    override val exitConditional: ConditionalNode? get() = null

    private fun hasJoin() = join != null || node.children.count { it.asType<Flow>()?.isContinuing() == true } > 0

    private val join: GatewaySymbol<*>?

    override fun isCompensating(): Boolean = (parent?.node as? CorrelatingFlow)?.isCompensating() == true

    init {

        task = when (node) {
            is Executing -> ServiceTaskSymbol(node, this)
            is Throwing -> SendTaskSymbol(node, this)
            is Consuming -> ReceiveTaskSymbol(node, this)
            else -> throw IllegalStateException()
        }

        join = if (hasJoin()) ExclusiveGatewaySymbol(node, this) else null

        elements = listOf(task) + node.children.map { Sequence(it as Flow, this) }  + listOfNotNull(join)

        if (join != null) {
            Path(task, join, this, null)
            sequences.filter { it.node.asType<Flow>()?.isContinuing() == true }.forEach {
                Path(it, join, it, it.exitConditional)
            }
        }

    }

    override fun initDiagram() {
        task.diagram.westEntryOf(diagram)
        assert(sequences.size <= 3) { "More than three boundary events are not supported for tasks." }
        sequences.getOrNull(0)?.diagram?.southOf(task.diagram)
        sequences.getOrNull(1)?.diagram?.southOf(sequences[0].diagram)
        sequences.getOrNull(2)?.diagram?.northOf(task.diagram)
        // sequences.getOrNull(3)?.diagram?.northOf(sequences[2].diagram) TODO()
        join?.diagram?.eastOf(task.diagram)
            ?: task.diagram.eastEntryOf(diagram)
    }

}