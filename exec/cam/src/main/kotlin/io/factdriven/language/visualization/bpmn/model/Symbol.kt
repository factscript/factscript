package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.Node
import io.factdriven.language.visualization.bpmn.diagram.Artefact
import io.factdriven.language.execution.cam.EngineStartListener
import org.camunda.bpm.model.bpmn.instance.*
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

    override fun initModel() {

        process.bpmnProcess.addChildElement(model)

        model.setAttributeValue("id", node.id, false)

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

    }

}