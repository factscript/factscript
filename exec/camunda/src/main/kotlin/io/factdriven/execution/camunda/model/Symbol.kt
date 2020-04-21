package io.factdriven.execution.camunda.model

import io.factdriven.definition.Node
import io.factdriven.execution.camunda.diagram.Direction
import io.factdriven.execution.camunda.diagram.Position
import io.factdriven.execution.camunda.engine.CamundaFlowNodeStartListener
import io.factdriven.execution.camunda.model.BpmnModel.Companion.margin
import org.camunda.bpm.model.bpmn.instance.*
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener
import org.camunda.bpm.model.bpmn.instance.dc.Bounds

abstract class Symbol<IN: Node, OUT: FlowNode>(node: IN, parent: Element<*,*>): Element<IN, OUT>(node, parent) {

    @Suppress("LeakingThis")
    override val children: List<Element<*,*>> = let {
        listOf(Label(node, this))
    }

    override fun entry(from: Direction): Position {
        return when(from) {
            Direction.North -> Position(
                dimension.width / 2,
                0
            ) south margin
            Direction.East -> Position(
                dimension.width,
                dimension.height / 2
            ) west margin
            Direction.South -> Position(
                dimension.width / 2,
                dimension.height
            ) north margin
            Direction.West -> Position(
                0,
                dimension.height / 2
            ) east margin
        }
    }

    override fun init() {

        process.bpmnProcess.addChildElement(model)

        model.setAttributeValue("id", node.id, false)

        val extensionElements = process.model.newInstance(ExtensionElements::class.java)
        model.addChildElement(extensionElements)

        with(extensionElements.addExtensionElement(CamundaExecutionListener::class.java)) {
            camundaClass = CamundaFlowNodeStartListener::class.java.canonicalName
            camundaEvent = "start"
        }

        val bpmnShape = process.model.newInstance(BpmnShape::class.java)
        bpmnShape.bpmnElement = model

        with(process.model.newInstance(Bounds::class.java)) {
            x = position.inner.x.toDouble()
            y = position.inner.y.toDouble()
            width = dimension.inner.width.toDouble()
            height = dimension.inner.height.toDouble()
            bpmnShape.bounds = this
        }

        process.bpmnPlane.addChildElement(bpmnShape)

    }

}