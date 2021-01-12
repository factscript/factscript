package org.factscript.language.visualization.bpmn.model

import org.factscript.language.definition.Node
import org.factscript.language.execution.cam.EngineStartListener
import org.factscript.language.visualization.bpmn.diagram.Artefact
import org.camunda.bpm.model.bpmn.instance.BoundaryEvent
import org.camunda.bpm.model.bpmn.instance.ExtensionElements
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener
import org.camunda.bpm.model.bpmn.instance.dc.Bounds

abstract class Symbol<IN: Node, OUT: FlowNode>(node: IN, override val parent: Group<*>): Element<IN, OUT>(node, parent) {

    @Suppress("LeakingThis")
    override val elements: List<Element<*,*>> = let {
        listOf(Label(node, this))
    }

    abstract override val diagram: Artefact

    override val west: Symbol<*, *> get() = this
    override val east: Symbol<*, *> get() = this

    override fun initDiagram() {}

    open val id: String get() = node.id
    open val description: String get() = node.description

    override fun initModel() {

        process.bpmnProcess.addChildElement(model)

        model.setAttributeValue("id", id, false)

        val extensionElements = process.model.newInstance(ExtensionElements::class.java)
        model.addChildElement(extensionElements)

        with(extensionElements.addExtensionElement(CamundaExecutionListener::class.java)) {
            camundaClass = EngineStartListener::class.java.canonicalName
            camundaEvent = "start"
        }

        val bpmnShape = process.model.newInstance(BpmnShape::class.java)
        bpmnShape.bpmnElement = model

        with(process.model.newInstance(Bounds::class.java)) {
            val raw = diagram.raw
            x = raw.position.x.toDouble()
            y = raw.position.y.toDouble()
            width = raw.dimension.width.toDouble()
            height = raw.dimension.height.toDouble()
            bpmnShape.bounds = this
        }

        process.bpmnPlane.addChildElement(bpmnShape)

        if (BpmnModel.renderGroups && !(model is BoundaryEvent)) {

            val groupModel = process.model.newInstance(org.camunda.bpm.model.bpmn.instance.Group::class.java)
            process.bpmnProcess.addChildElement(groupModel)
            val groupBpmnShape = process.model.newInstance(BpmnShape::class.java)
            groupBpmnShape.bpmnElement = groupModel
            process.bpmnProcess.diagramElement.addChildElement(groupBpmnShape)

            with(process.model.newInstance(Bounds::class.java)) {
                x = diagram.position.x.toDouble()
                y = diagram.position.y.toDouble()
                width = diagram.dimension.width.toDouble()
                height = diagram.dimension.height.toDouble()
                groupBpmnShape.bounds = this
            }

        }

    }

}