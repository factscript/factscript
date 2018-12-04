package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.*
import java.lang.IllegalArgumentException

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun translate(node: FlowNode): GraphicalFlowNode {
    val graphicalNode = when(node){
        is FlowReactionImpl<*, *> -> GraphicalNoneStartEvent(node.name)
        is FlowExecutionImpl<*> -> {
            when (node.type) {
                FlowDefinitionType.execution -> {
                    val definition = node as FlowDefinition<*>
                    val sequence = GraphicalFlowNodeSequence(node.name)
                    definition.nodes.forEach {
                        sequence.add(translate(it))
                    }
                    return sequence
                }
                else -> GraphicalServiceTask(node.name)
            }
        }
        is FlowActionImpl<*, *> -> GraphicalNoneEndEvent(node.name)
        else -> throw IllegalArgumentException()
    }
    return graphicalNode
}
