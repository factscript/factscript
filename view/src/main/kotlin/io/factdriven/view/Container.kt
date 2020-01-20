package io.factdriven.view

import io.factdriven.def.*
import io.factdriven.flow.view.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun translate(definition: Definition): Container {

    fun translate(element: Node, parent: Container): Element {
        return when(element) {
            is Catching -> {
                if (element.isFirstChild) {
                    BpmnEventSymbol(
                        element.id,
                        element.typeName,
                        parent,
                        BpmnEventType.message,
                        BpmnEventCharacteristic.catching
                    )
                } else {
                    BpmnTaskSymbol(
                        element.id,
                        element.typeName,
                        parent,
                        BpmnTaskType.receive
                    )
                }
            }
            is Throwing -> {
                BpmnEventSymbol(
                    element.id,
                    element.typeName,
                    parent,
                    BpmnEventType.message,
                    BpmnEventCharacteristic.throwing
                )
            }
            else -> throw IllegalArgumentException()
        }
    }

    val sequence = Sequence(definition.id, definition.typeName)
    definition.children.forEach { translate(it, sequence) }
    return sequence

}
