package io.factdriven.flowlang.view

import io.factdriven.flow.lang.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun translate(flowExecution: FlowExecution<*>): Container {

    fun translate(node: FlowNode, parent: Container): Element {
        return when(node) {
            is FlowReactionImpl<*, *> -> {
                BpmnEventSymbol(node.id, parent, BpmnEventType.message, BpmnEventCharacteristic.catching)
            }
            is FlowExecutionImpl<*> -> {
                when (node.type) {
                    FlowDefinitionType.execution -> {
                        val definition = node as FlowDefinition<*>
                        val sequence = Sequence(node.id, parent)
                        definition.nodes.forEach { translate(it, sequence) }
                        sequence
                    }
                    else -> {
                        val children = (node as FlowDefinition<*>).nodes
                        val type = if (children.isEmpty()) BpmnTaskType.service
                            else if (children.first() is FlowReactionImpl<*,*>) BpmnTaskType.receive
                            else if (children.size == 1) BpmnTaskType.send
                            else BpmnTaskType.service
                        BpmnTaskSymbol(node.id, parent, type)
                    }
                }
            }
            is FlowActionImpl<*, *> -> {
                BpmnEventSymbol(node.id, parent, if (node.action != null) BpmnEventType.message else BpmnEventType.none, BpmnEventCharacteristic.throwing)
            }
            else -> throw IllegalArgumentException()
        }
    }

    val definition = flowExecution as FlowDefinition<*>
    val sequence = Sequence(flowExecution.id)
    definition.nodes.forEach { translate(it, sequence) }
    return sequence

}
