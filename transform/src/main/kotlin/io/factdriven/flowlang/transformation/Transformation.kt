package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.Definition
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
import java.io.File
import org.camunda.bpm.model.bpmn.instance.EndEvent
import org.camunda.bpm.model.bpmn.instance.StartEvent
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram
import org.camunda.bpm.model.bpmn.instance.dc.Bounds
import java.io.IOException




/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun transform(flow: Definition<*>): BpmnModelInstance {

    val process = createElement(id = "process", elementClass = Process::class.java)

    val startEvent = createElement(process, "start", StartEvent::class.java)
    val task1 = createElement(process, "task1", ServiceTask::class.java)
    task1.name = "User Task"
    val endEvent = createElement(process, "end", EndEvent::class.java)

    createSequenceFlow(process, startEvent, task1)
    createSequenceFlow(process, task1, endEvent)

    return process.modelInstance as BpmnModelInstance

}

fun createModelInstance(): BpmnModelInstance {

    val modelInstance = Bpmn.createEmptyModel()
    val definitions = modelInstance.newInstance(Definitions::class.java)
    definitions.targetNamespace = "http://camunda.org/examples"
    modelInstance.definitions = definitions
    return modelInstance

}

fun <T : BpmnModelElementInstance> createElement(
    parentElement: BpmnModelElementInstance = createModelInstance().definitions,
    id: String,
    elementClass: Class<T>
): T {

    val element = parentElement.modelInstance.newInstance(elementClass)
    element.setAttributeValue("id", id, true)
    parentElement.addChildElement(element)
    return element

}

fun createSequenceFlow(process: Process, from: FlowNode, to: FlowNode): SequenceFlow {
    val identifier = from.id + "-" + to.id
    val sequenceFlow = createElement(process, identifier, SequenceFlow::class.java)
    process.addChildElement(sequenceFlow)
    sequenceFlow.source = from
    from.outgoing.add(sequenceFlow)
    sequenceFlow.target = to
    to.incoming.add(sequenceFlow)
    return sequenceFlow
}

class ProcessGenerator {

    lateinit var modelInstance: BpmnModelInstance

    @Throws(IOException::class)
    fun generateProcess() {
        modelInstance = Bpmn.createEmptyModel()
        val definitions = modelInstance.newInstance(Definitions::class.java)
        definitions.targetNamespace = "http://camunda.org/examples"
        modelInstance.definitions = definitions

        // create the process
        val process = modelInstance.newInstance(Process::class.java)
        process.setAttributeValue("id", "process-one-task", true)
        definitions.addChildElement(process)

        val diagram = modelInstance.newInstance(BpmnDiagram::class.java)
        val plane = modelInstance.newInstance(BpmnPlane::class.java)
        plane.bpmnElement = process
        diagram.bpmnPlane = plane
        definitions.addChildElement(diagram)

        // create start event, user task and end event
        val startEvent = createElement(
            process, "start", "Retrieve payment",
            StartEvent::class.java, plane, 122.0, 102.0, 36.0, 36.0, true
        )

        val userTask = createElement(
            process, "serviceTask", "Charge credit card",
            ServiceTask::class.java, plane, 194.0, 80.0, 80.0, 100.0, false
        )

        createSequenceFlow(process, startEvent, userTask, plane, 158, 120, 194, 120)

        val endEvent = createElement(
            process, "end", "Payment retrieved",
            EndEvent::class.java, plane, 330.0, 102.0, 36.0, 36.0, true
        )

        createSequenceFlow(process, userTask, endEvent, plane, 294, 120, 330, 120)

        // validate and write model to file
        Bpmn.validateModel(modelInstance)
        val file = File.createTempFile("bpmn-model-api-", ".bpmn")
        Bpmn.writeModelToFile(file, modelInstance)

    }

    protected fun <T : BpmnModelElementInstance> createElement(
        parentElement: BpmnModelElementInstance,
        id: String, name: String, elementClass: Class<T>, plane: BpmnPlane,
        x: Double, y: Double, heigth: Double, width: Double, withLabel: Boolean
    ): T {
        val element = modelInstance.newInstance(elementClass)
        element.setAttributeValue("id", id, true)
        element.setAttributeValue("name", name, false)
        parentElement.addChildElement(element)

        val bpmnShape = modelInstance.newInstance(BpmnShape::class.java)
        bpmnShape.bpmnElement = element as BaseElement

        val bounds = modelInstance.newInstance(Bounds::class.java)
        bounds.setX(x)
        bounds.setY(y)
        bounds.setHeight(heigth)
        bounds.setWidth(width)
        bpmnShape.bounds = bounds

        if (withLabel) {
            val bpmnLabel = modelInstance.newInstance(BpmnLabel::class.java)
            val labelBounds = modelInstance.newInstance(Bounds::class.java)
            labelBounds.setX(x + 3)
            labelBounds.setY(y + heigth + 6)
            labelBounds.setHeight(heigth)
            labelBounds.setWidth(width - 6)
            bpmnLabel.addChildElement(labelBounds)
            bpmnShape.addChildElement(bpmnLabel)
        }
        plane.addChildElement(bpmnShape)

        return element
    }

    fun createSequenceFlow(
        process: org.camunda.bpm.model.bpmn.instance.Process, from: FlowNode, to: FlowNode, plane: BpmnPlane,
        vararg waypoints: Int
    ): SequenceFlow {
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

        return sequenceFlow
    }

    companion object {

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val createProcess = ProcessGenerator()
            createProcess.generateProcess()
        }
    }

}