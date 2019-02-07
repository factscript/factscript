package io.factdriven.flow.view

import io.factdriven.flow.lang.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun translate(execution: Flow<*>): Container {
    return translate(execution.asDefinition())
}

fun translate(definition: DefinedFlow<*>): Container {

    fun translate(element: Node, parent: Container): Element {
        return when(element) {
            is DefinedReaction -> {
                BpmnEventSymbol(
                    element.id,
                    element.name,
                    parent,
                    BpmnEventType.message,
                    BpmnEventCharacteristic.catching
                )
            }
            is DefinedFlow<*> -> {
                when (element.classifier) {
                    FlowClassifier.Execution -> {
                        val sequence = Sequence(element.id, element.name, parent)
                        element.children.forEach { translate(it, sequence) }
                        sequence
                    }
                    else -> {
                        val type = if (element.children.isEmpty()) BpmnTaskType.service
                            else if (element.children.first() is FlowReactionImpl<*,*,*>) BpmnTaskType.receive
                            else if (element.children.size == 1) BpmnTaskType.send
                            else BpmnTaskType.service
                        BpmnTaskSymbol(
                            element.id,
                            element.name,
                            parent,
                            type
                        )
                    }
                }
            }
            is DefinedAction -> {
                BpmnEventSymbol(
                    element.id,
                    element.name,
                    parent,
                    if (element.function != null) BpmnEventType.message else BpmnEventType.none,
                    BpmnEventCharacteristic.throwing
                )
            }
            else -> throw IllegalArgumentException()
        }
    }

    val sequence = Sequence(definition.id, definition.name)
    definition.children.forEach { translate(it, sequence) }
    return sequence

}
