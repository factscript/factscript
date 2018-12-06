package io.factdriven.flowlang.view

import io.factdriven.flow.lang.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun translate(definition: FlowExecutionDefinition): Container {

    fun translate(element: FlowElement, parent: Container): Element {
        return when(element) {
            is FlowReactionDefinition -> {
                BpmnEventSymbol(element.name, parent, BpmnEventType.message, BpmnEventCharacteristic.catching)
            }
            is FlowExecutionDefinition -> {
                when (element.executionType) {
                    FlowExecutionType.execution -> {
                        val sequence = Sequence(element.name, parent)
                        element.elements.forEach { translate(it, sequence) }
                        sequence
                    }
                    else -> {
                        val type = if (element.elements.isEmpty()) BpmnTaskType.service
                            else if (element.elements.first() is FlowReactionImpl<*,*>) BpmnTaskType.receive
                            else if (element.elements.size == 1) BpmnTaskType.send
                            else BpmnTaskType.service
                        BpmnTaskSymbol(element.name, parent, type)
                    }
                }
            }
            is FlowActionDefinition -> {
                BpmnEventSymbol(element.name, parent, if (element.function != null) BpmnEventType.message else BpmnEventType.none, BpmnEventCharacteristic.throwing)
            }
            else -> throw IllegalArgumentException()
        }
    }

    val sequence = Sequence(definition.name)
    definition.elements.forEach { translate(it, sequence) }
    return sequence

}
