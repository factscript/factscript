package io.factdriven.execution.camunda.model

import io.factdriven.definition.Node
import org.camunda.bpm.model.bpmn.instance.Group
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape
import org.camunda.bpm.model.bpmn.instance.dc.Bounds

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class Group<IN: Node>(node: IN, parent: Element<*,*>): Element<IN, Group>(node, parent) {

    override val model: Group = process.model.newInstance(Group::class.java)

    override fun init() {

        if (BpmnModel.groups) {

            process.bpmnProcess.addChildElement(model)
            val bpmnShape = process.model.newInstance(BpmnShape::class.java)
            bpmnShape.bpmnElement = model
            process.bpmnProcess.diagramElement.addChildElement(bpmnShape)

            with(process.model.newInstance(Bounds::class.java)) {
                x = position.x.toDouble()
                y = position.y.toDouble()
                width = dimension.width.toDouble()
                height = dimension.height.toDouble()
                bpmnShape.bounds = this
            }

        }

    }

}