package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun translate(flowExecution: FlowExecution<*>): Container {

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
