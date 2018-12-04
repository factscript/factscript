package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.*
import java.lang.IllegalArgumentException

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun translate(node: FlowNode): GraphicalFlowNode {
    val graphicalNode = when(node){
        is FlowReactionImpl<*, *> -> GraphicalNoneStartEvent(node.label)
        is FlowExecutionImpl<*> -> {
            when (node.type) {
                FlowDefinitionType.execution -> {
                    val definition = node as FlowDefinition<*>
                    val sequence = GraphicalFlowNodeSequence(node.label)
                    definition.nodes.forEach {
                        sequence.add(translate(it))
                    }
                    return sequence
                }
                else -> GraphicalServiceTask(node.label)
            }
        }
        is FlowActionImpl<*, *> -> GraphicalNoneEndEvent(node.label)
        else -> throw IllegalArgumentException()
    }
    return graphicalNode
}
