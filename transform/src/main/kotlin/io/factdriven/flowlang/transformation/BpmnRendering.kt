package io.factdriven.flowlang.transformation

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

val zero = Position(100,60)

abstract class BpmnSymbol<M: FlowNode>(id: Id, parent: Container): Symbol(id, parent) {

    abstract val elementClass: KClass<out M>

}

abstract class BpmnTaskSymbol<M: FlowNode>(id: Id, parent: Container): BpmnSymbol<M>(id, parent)  {

    override val inner = Dimension(100,80)

}

abstract class BpmnEventSymbol<M: FlowNode>(id: Id, parent: Container): BpmnSymbol<M>(id, parent) {

    override val inner = Dimension(36,36)

}

class BpmnServiceTaskSymbol(id: Id, parent: Container): BpmnTaskSymbol<ServiceTask>(id, parent) {

    override val elementClass = ServiceTask::class

}

class BpmnStartEventSymbol(id: Id, parent: Container): BpmnEventSymbol<StartEvent>(id, parent) {

    override val elementClass = StartEvent::class

}

class BpmnEndEventSymbol(id: Id, parent: Container): BpmnEventSymbol<StartEvent>(id, parent) {

    override val elementClass = StartEvent::class

}

fun transform(container: Container): BpmnModelInstance {

    val modelInstance = Bpmn.createEmptyModel()

    val definitions = modelInstance.newInstance(Definitions::class.java)
    definitions.targetNamespace = "http://camunda.org/examples"
    modelInstance.definitions = definitions

    val process = modelInstance.newInstance(Process::class.java)
    process.setAttributeValue("id", container.id.key, true)
    process.setAttributeValue("name", container.id.label, true)
    definitions.addChildElement(process)

    val plane = modelInstance.newInstance(BpmnPlane::class.java)
    plane.bpmnElement = process

    val diagram = modelInstance.newInstance(BpmnDiagram::class.java)
    diagram.bpmnPlane = plane
    definitions.addChildElement(diagram)

    val map = mutableMapOf<Symbol, FlowNode>()

    fun <M: FlowNode> createBpmnModelElementInstance(symbol: BpmnSymbol<M>): M {

        val modelElementInstance = modelInstance.newInstance(symbol.elementClass.java)
        modelElementInstance.setAttributeValue("id", symbol.id.key, true)
        modelElementInstance.setAttributeValue("name", symbol.id.label, false)
        process.addChildElement(modelElementInstance)

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
        sequenceFlow.setAttributeValue("id", connector.id.key, true)
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

    container.symbols.forEach { map[it] = createBpmnModelElementInstance(it as BpmnSymbol<*>) }
    container.connectors.forEach { createSequenceFlow(it) }

    return process.modelInstance as BpmnModelInstance

}
