package io.factdriven.flow.view

import io.factdriven.flow.lang.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun translate(execution: FlowExecution<*>): Container {
    return translate(execution.asDefinition())
}

fun translate(definition: FlowDefinition): Container {

    fun translate(element: FlowElement, parent: Container): Element {
        return when(element) {
            is FlowReactionDefinition -> {
                BpmnEventSymbol(
                    element.flowElementId,
                    element.flowElementType,
                    parent,
                    BpmnEventType.message,
                    BpmnEventCharacteristic.catching
                )
            }
            is FlowDefinition -> {
                when (element.flowExecutionType) {
                    FlowExecutionType.execution -> {
                        val sequence = Sequence(element.flowElementId, element.flowElementType, parent)
                        element.children.forEach { translate(it, sequence) }
                        sequence
                    }
                    else -> {
                        val type = if (element.children.isEmpty()) BpmnTaskType.service
                            else if (element.children.first() is FlowReactionImpl<*,*,*>) BpmnTaskType.receive
                            else if (element.children.size == 1) BpmnTaskType.send
                            else BpmnTaskType.service
                        BpmnTaskSymbol(
                            element.flowElementId,
                            element.flowElementType,
                            parent,
                            type
                        )
                    }
                }
            }
            is FlowActionDefinition -> {
                BpmnEventSymbol(
                    element.flowElementId,
                    element.flowElementType,
                    parent,
                    if (element.function != null) BpmnEventType.message else BpmnEventType.none,
                    BpmnEventCharacteristic.throwing
                )
            }
            else -> throw IllegalArgumentException()
        }
    }

    val sequence = Sequence(definition.flowElementId, definition.flowElementType)
    definition.children.forEach { translate(it, sequence) }
    return sequence

}
