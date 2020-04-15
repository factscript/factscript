package io.factdriven.execution.camunda.model

import io.factdriven.definition.Node
import io.factdriven.execution.camunda.engine.CamundaFlowNodeStartListener
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
            Direction.North -> Position(dimension.width / 2, 0)
            Direction.East -> Position(dimension.width, dimension.height / 2)
            Direction.South -> Position(dimension.width / 2, dimension.height)
            Direction.West -> Position(0, dimension.height / 2)
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

        val innerPosition = Position(position.x + BpmnModel.margin.height, position.y + BpmnModel.margin.height)
        val innerDimension = Dimension(dimension.width - 2 * BpmnModel.margin.width, dimension.height - 2 * BpmnModel.margin.height)

        with(process.model.newInstance(Bounds::class.java)) {
            x = innerPosition.x.toDouble()
            y = innerPosition.y.toDouble()
            width = innerDimension.width.toDouble()
            height = innerDimension.height.toDouble()
            bpmnShape.bounds = this
        }

        process.bpmnPlane.addChildElement(bpmnShape)

    }

}