package io.factdriven.view

import io.factdriven.def.*
import io.factdriven.flow.view.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun translate(definition: Definition): Container {

    fun translate(node: Node, parent: Container): Element {
        return when(node) {
            is Catching -> {
                if (node.isFirstChild) {
                    BpmnEventSymbol(
                        node.id,
                        node.typeName,
                        parent,
                        BpmnEventType.message,
                        BpmnEventCharacteristic.catching
                    )
                } else {
                    BpmnTaskSymbol(
                        node.id,
                        node.typeName,
                        parent,
                        BpmnTaskType.receive
                    )
                }
            }
            is Throwing -> {
                BpmnEventSymbol(
                    node.id,
                    node.typeName,
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
