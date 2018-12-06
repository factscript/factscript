package io.factdriven.flowlang.view

import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.*
import org.camunda.bpm.model.bpmn.instance.bpmndi.*
import org.camunda.bpm.model.bpmn.instance.dc.Bounds
import org.camunda.bpm.model.bpmn.instance.di.Waypoint
import kotlin.reflect.KClass


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

val zero = Position(173 - margin, 80 - margin)

abstract class BpmnSymbol(name: String, parent: Container): Symbol(name, parent) {

    abstract val elementClass: KClass<out FlowNode>

}

class BpmnTaskSymbol(name: String, parent: Container, val type: BpmnTaskType): BpmnSymbol(name, parent)  {

    override val inner = Dimension(100,80)

    override val elementClass: KClass<out FlowNode> get() {
        return when (type) {
            BpmnTaskType.service -> ServiceTask::class
            BpmnTaskType.send -> SendTask::class
            BpmnTaskType.receive -> ReceiveTask::class
        }
    }

}

enum class BpmnTaskType {
    service, send, receive
}

enum class BpmnEventType {
    none, message
}

enum class BpmnEventCharacteristic {
    throwing, catching
}

enum class BpmnEventPosition {
    start, intermediate, end
}

class BpmnEventSymbol(name: String, parent: Container, val type: BpmnEventType, val characteristic: BpmnEventCharacteristic): BpmnSymbol(name, parent) {

    override val inner = Dimension(36,36)

    override val name: String = name
        get() = field.replace(" ", "\n")

    override val elementClass: KClass<out FlowNode> get() {
        return when (characteristic) {
            BpmnEventCharacteristic.throwing -> {
                when (position()) {
                    BpmnEventPosition.intermediate -> IntermediateThrowEvent::class
                    BpmnEventPosition.end -> EndEvent::class
                    else -> throw IllegalStateException()
                }
            }
            BpmnEventCharacteristic.catching -> {
                when (position()) {
                    BpmnEventPosition.start -> StartEvent::class
                    BpmnEventPosition.intermediate -> IntermediateCatchEvent::class
                    else -> throw IllegalStateException()
                }
            }
        }
    }

    private fun position(): BpmnEventPosition {
        return if (incoming.isEmpty()) {
            BpmnEventPosition.start
        } else if (outgoing.isEmpty()) {
            BpmnEventPosition.end
        } else {
            BpmnEventPosition.intermediate
        }
    }

}

fun transform(container: Container): BpmnModelInstance {

    val modelInstance = Bpmn.createEmptyModel()

    val definitions = modelInstance.newInstance(Definitions::class.java)
    definitions.targetNamespace = "https://factdriven.io/tests"
    modelInstance.definitions = definitions

    val process = modelInstance.newInstance(Process::class.java)
    process.setAttributeValue("id", container.id, true)
    process.setAttributeValue("name", container.name, true)
    process.isExecutable = true
    definitions.addChildElement(process)

    val plane = modelInstance.newInstance(BpmnPlane::class.java)
    plane.bpmnElement = process

    val diagram = modelInstance.newInstance(BpmnDiagram::class.java)
    diagram.bpmnPlane = plane
    definitions.addChildElement(diagram)

    val map = mutableMapOf<Symbol, FlowNode>()

    fun createBpmnModelElementInstance(symbol: BpmnSymbol): FlowNode {

        val modelElementInstance = modelInstance.newInstance(symbol.elementClass.java)
        modelElementInstance.setAttributeValue("id", symbol.id, true)
        modelElementInstance.setAttributeValue("name", symbol.name, false)
        process.addChildElement(modelElementInstance)

        if (symbol is BpmnEventSymbol) {
            when (symbol.type) {
                BpmnEventType.message -> {
                    val messageEventDefinition = modelInstance.newInstance(MessageEventDefinition::class.java)
                    modelElementInstance.addChildElement(messageEventDefinition)
                }
            }
        }

        val bpmnShape = modelInstance.newInstance(BpmnShape::class.java)
        bpmnShape.bpmnElement = modelElementInstance as BaseElement

        val bounds = modelInstance.newInstance(Bounds::class.java)
        bounds.setX((symbol.topLeft.x + zero.x).toDouble())
        bounds.setY((symbol.topLeft.y + zero.y).toDouble())
        bounds.setWidth(symbol.inner.width.toDouble())
        bounds.setHeight(symbol.inner.height.toDouble())
        bpmnShape.bounds = bounds

        val bpmnLabel = modelInstance.newInstance(BpmnLabel::class.java)
        val labelBounds = modelInstance.newInstance(Bounds::class.java)
        labelBounds.setX((symbol.topLeft.x + zero.x).toDouble() + 6)
        labelBounds.setY((symbol.topLeft.y + zero.y).toDouble() + symbol.inner.height + 6)
        labelBounds.setHeight(20.toDouble()) // TODO adapt according to text size
        labelBounds.setWidth(symbol.inner.width.toDouble() - 12)
        bpmnLabel.addChildElement(labelBounds)
        bpmnShape.addChildElement(bpmnLabel)

        plane.addChildElement(bpmnShape)

        return modelElementInstance

    }

    fun createSequenceFlow(connector: Connector) {

        val sequenceFlow = modelInstance.newInstance(SequenceFlow::class.java)
        sequenceFlow.setAttributeValue("id", connector.id, true)
        process.addChildElement(sequenceFlow)

        sequenceFlow.source = map[connector.source]
        sequenceFlow.source.outgoing.add(sequenceFlow)

        sequenceFlow.target = map[connector.target]
        sequenceFlow.target.incoming.add(sequenceFlow)

        val bpmnEdge = modelInstance.newInstance(BpmnEdge::class.java)
        bpmnEdge.bpmnElement = sequenceFlow

        connector.waypoints.forEach {
            val waypoint = modelInstance.newInstance(Waypoint::class.java)
            waypoint.x = (it.x + zero.x).toDouble()
            waypoint.y = (it.y + zero.y).toDouble()
            bpmnEdge.addChildElement(waypoint)
        }

        plane.addChildElement(bpmnEdge)

    }

    container.symbols.forEach { map[it] = createBpmnModelElementInstance(it as BpmnSymbol) }
    container.connectors.forEach { createSequenceFlow(it) }

    return process.modelInstance as BpmnModelInstance

}
