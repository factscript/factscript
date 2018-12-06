package io.factdriven.flowlang.view

import io.factdriven.flow.lang.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun translate(flowExecution: FlowExecution<*>): Container {

    fun translate(node: FlowNode, parent: Container): Element {
        return when(node) {
            is FlowReactionImpl<*, *> -> {
                BpmnEventSymbol(Id(node.id), parent, BpmnEventType.message, BpmnEventCharacteristic.catching)
            }
            is FlowExecutionImpl<*> -> {
                when (node.type) {
                    FlowDefinitionType.execution -> {
                        val definition = node as FlowDefinition<*>
                        val sequence = Sequence(Id(node.id), parent)
                        definition.nodes.forEach { translate(it, sequence) }
                        sequence
                    }
                    else -> BpmnTaskSymbol(Id(node.id), parent)
                }
            }
            is FlowActionImpl<*, *> -> {
                BpmnEventSymbol(Id(node.id), parent, BpmnEventType.message, BpmnEventCharacteristic.throwing)
            }
            else -> throw IllegalArgumentException()
        }
    }

    val definition = flowExecution as FlowDefinition<*>
    val sequence = Sequence(Id(flowExecution.id))
    definition.nodes.forEach { translate(it, sequence) }
    return sequence

}
