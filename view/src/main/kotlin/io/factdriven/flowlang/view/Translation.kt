package io.factdriven.flowlang.view

import io.factdriven.flow.lang.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun translate(flowExecution: FlowExecution<*>): Container {

    fun translate(node: FlowNode, parent: Container): Element {
        return when(node) {
            is FlowReactionImpl<*, *> -> {
                val element = BpmnStartEventSymbol(Id(node.id), parent)
                element.message = node.reactionType == FlowReactionType.message
                element
            }
            is FlowExecutionImpl<*> -> {
                when (node.type) {
                    FlowDefinitionType.execution -> {
                        val definition = node as FlowDefinition<*>
                        val sequence = Sequence(Id(node.id), parent)
                        definition.nodes.forEach { translate(it, sequence) }
                        sequence
                    }
                    else -> BpmnServiceTaskSymbol(Id(node.id), parent)
                }
            }
            is FlowActionImpl<*, *> -> {
                val element = BpmnEndEventSymbol(Id(node.id), parent)
                element.message = node.action != null
                element
            }
            else -> throw IllegalArgumentException()
        }
    }

    val definition = flowExecution as FlowDefinition<*>
    val sequence = Sequence(Id(flowExecution.id))
    definition.nodes.forEach { translate(it, sequence) }
    return sequence

}
