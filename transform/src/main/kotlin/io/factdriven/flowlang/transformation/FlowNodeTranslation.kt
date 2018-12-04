package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.*
import java.lang.IllegalArgumentException

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun translate(node: FlowNode): GraphicalElement {
    val graphicalNode = when(node) {
        is FlowReactionImpl<*, *> -> RenderedNoneStartEvent(node.name) as GraphicalElement
        is FlowExecutionImpl<*> -> {
            when (node.type) {
                FlowDefinitionType.execution -> {
                    val definition = node as FlowDefinition<*>
                    val sequence = GraphicalElementSequence(node.name)
                    definition.nodes.forEach {
                        sequence.add(translate(it))
                    }
                    return sequence
                }
                else -> RenderedServiceTask(node.name) as GraphicalElement
            }
        }
        is FlowActionImpl<*, *> -> RenderedNoneEndEvent(node.name) as GraphicalElement
        else -> throw IllegalArgumentException()
    }
    return graphicalNode
}
