package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.FlowExecution
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.*
import org.camunda.bpm.model.bpmn.instance.di.Waypoint
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge
import org.camunda.bpm.model.bpmn.instance.SequenceFlow
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnLabel
import org.camunda.bpm.model.bpmn.instance.BaseElement
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance
import org.camunda.bpm.model.bpmn.instance.EndEvent
import org.camunda.bpm.model.bpmn.instance.StartEvent
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram
import org.camunda.bpm.model.bpmn.instance.dc.Bounds
import java.lang.IllegalArgumentException
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

fun transform(flow: FlowExecution<*>): BpmnModelInstance {

    val rendered = translate(flow).rendered()

    val modelInstance = Bpmn.createEmptyModel()
    val definitions = modelInstance.newInstance(Definitions::class.java)
    definitions.targetNamespace = "http://camunda.org/examples"
    modelInstance.definitions = definitions
    val process = modelInstance.newInstance(Process::class.java)
    process.setAttributeValue("id", flow.id, true)
    definitions.addChildElement(process)
    val diagram = modelInstance.newInstance(BpmnDiagram::class.java)
    val plane = modelInstance.newInstance(BpmnPlane::class.java)
    plane.bpmnElement = process
    diagram.bpmnPlane = plane
    definitions.addChildElement(diagram)

    val map = mutableMapOf<Rendered, FlowNode>()

    rendered.forEach {

        when(it) {
            is RenderedStartEvent -> map[it] = createElement(modelInstance, it.id, name(it.id), process, StartEvent::class.java, plane, it.renderingPosition.x + x, it.renderingPosition.y + y, it.renderingDimension.width, it.renderingDimension.height)
            is RenderedEndEvent -> map[it] = createElement(modelInstance, it.id, name(it.id), process, EndEvent::class.java, plane, it.renderingPosition.x + x, it.renderingPosition.y + y, it.renderingDimension.width, it.renderingDimension.height)
            is RenderedServiceTask -> map[it] = createElement(modelInstance, it.id, name(it.id), process, ServiceTask::class.java, plane, it.renderingPosition.x + x, it.renderingPosition.y + y, it.renderingDimension.width, it.renderingDimension.height)
            is RenderedSequenceFlow -> createSequenceFlow(modelInstance, process, map[it.source]!!, map[it.target]!!, plane, it.from.x + x, it.from.y + y, it.to.x + x, it.to.y + y)
            else -> throw IllegalArgumentException()
        }

    }

    return process.modelInstance as BpmnModelInstance

}

const val x = 100
const val y = 70

fun <T : BpmnModelElementInstance> createElement(modelInstance: BpmnModelInstance,
    id: String, name: String, parentElement: BpmnModelElementInstance, elementClass: Class<T>, plane: BpmnPlane,
    x: Int, y: Int, width: Int, heigth: Int, withLabel: Boolean = true
): T {
    val element = modelInstance.newInstance(elementClass)
    element.setAttributeValue("id", id, true)
    element.setAttributeValue("name", name, false)
    parentElement.addChildElement(element)

    val bpmnShape = modelInstance.newInstance(BpmnShape::class.java)
    bpmnShape.bpmnElement = element as BaseElement

    val bounds = modelInstance.newInstance(Bounds::class.java)
    bounds.setX(x.toDouble())
    bounds.setY(y.toDouble())
    bounds.setHeight(heigth.toDouble())
    bounds.setWidth(width.toDouble())
    bpmnShape.bounds = bounds

    if (withLabel) {
        val bpmnLabel = modelInstance.newInstance(BpmnLabel::class.java)
        val labelBounds = modelInstance.newInstance(Bounds::class.java)
        labelBounds.setX(x.toDouble() + 3)
        labelBounds.setY(y.toDouble() + heigth + 6)
        labelBounds.setHeight(heigth.toDouble())
        labelBounds.setWidth(width.toDouble() - 6)
        bpmnLabel.addChildElement(labelBounds)
        bpmnShape.addChildElement(bpmnLabel)
    }
    plane.addChildElement(bpmnShape)

    return element
}

fun createSequenceFlow(modelInstance: BpmnModelInstance,
    process: org.camunda.bpm.model.bpmn.instance.Process, from: FlowNode, to: FlowNode, plane: BpmnPlane,
    vararg waypoints: Int
) {
    val identifier = from.id + "-" + to.id
    val sequenceFlow = modelInstance.newInstance(SequenceFlow::class.java)
    sequenceFlow.setAttributeValue("id", identifier, true)
    process.addChildElement(sequenceFlow)
    sequenceFlow.source = from
    from.outgoing.add(sequenceFlow)
    sequenceFlow.target = to
    to.incoming.add(sequenceFlow)

    val bpmnEdge = modelInstance.newInstance(BpmnEdge::class.java)
    bpmnEdge.bpmnElement = sequenceFlow
    for (i in 0 until waypoints.size / 2) {
        val waypointX = waypoints[i * 2].toDouble()
        val waypointY = waypoints[i * 2 + 1].toDouble()
        val wp = modelInstance.newInstance(Waypoint::class.java)
        wp.x = waypointX
        wp.y = waypointY
        bpmnEdge.addChildElement(wp)
    }
    plane.addChildElement(bpmnEdge)
}

fun name(id: String): String {
    val regex = String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])").toRegex()
    val split = id.split(regex)
    return (split[0] + "\n" + if (split.size > 1) split.subList(1, split.size).joinToString(separator = "") { it.substring(0, 1).toLowerCase() + it.substring(1) + " " } else "").trim()
}
