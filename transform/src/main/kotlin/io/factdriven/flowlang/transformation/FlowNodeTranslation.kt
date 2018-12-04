package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.*
import java.lang.IllegalArgumentException

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
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
