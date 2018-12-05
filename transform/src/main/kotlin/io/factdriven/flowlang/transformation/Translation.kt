package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.*
import java.lang.IllegalArgumentException

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun transform2(flowExecution: FlowExecution<*>): Container {

    fun translate(node: FlowNode, parent: Container): Element {
        return when(node) {
            is FlowReactionImpl<*, *> -> BpmnStartEventSymbol(Id(node.id), parent)
            is FlowExecutionImpl<*> -> {
                when (node.type) {
                    FlowDefinitionType.execution -> {
                        val definition = node as FlowDefinition<*>
                        val sequence = Sequence(Id(node.id), parent)
                        definition.nodes.forEach { translate(it, sequence) }
                        return sequence
                    }
                    else -> BpmnServiceTaskSymbol(Id(node.id), parent)
                }
            }
            is FlowActionImpl<*, *> -> BpmnEndEventSymbol(Id(node.id), parent)
            else -> throw IllegalArgumentException()
        }
    }

    val definition = flowExecution as FlowDefinition<*>
    val sequence = Sequence(Id(flowExecution.id))
    definition.nodes.forEach { translate(it, sequence) }
    return sequence

}

fun translate(node: FlowNode): GraphicalElement {
    val graphicalNode = when(node) {
        is FlowReactionImpl<*, *> -> RenderedStartEvent(node.id) as GraphicalElement
        is FlowExecutionImpl<*> -> {
            when (node.type) {
                FlowDefinitionType.execution -> {
                    val definition = node as FlowDefinition<*>
                    val sequence = GraphicalElementSequence(node.id)
                    definition.nodes.forEach {
                        sequence.add(translate(it))
                    }
                    return sequence
                }
                else -> RenderedServiceTask(node.id) as GraphicalElement
            }
        }
        is FlowActionImpl<*, *> -> RenderedEndEvent(node.id) as GraphicalElement
        else -> throw IllegalArgumentException()
    }
    return graphicalNode
}
