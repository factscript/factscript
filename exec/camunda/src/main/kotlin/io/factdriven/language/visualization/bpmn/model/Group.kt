package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.Node
import io.factdriven.language.visualization.bpmn.diagram.Container
import org.camunda.bpm.model.bpmn.instance.Group
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape
import org.camunda.bpm.model.bpmn.instance.dc.Bounds

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class Group<IN: Node>(node: IN, parent: Element<*,*>): Element<IN, Group>(node, parent) {

    override val model: Group = process.model.newInstance(Group::class.java)

    override val diagram: Container = Container(36)

    override fun initModel() {

        if (BpmnModel.groups) {

            process.bpmnProcess.addChildElement(model)
            val bpmnShape = process.model.newInstance(BpmnShape::class.java)
            bpmnShape.bpmnElement = model
            process.bpmnProcess.diagramElement.addChildElement(bpmnShape)

            with(process.model.newInstance(Bounds::class.java)) {
                x = diagram.position.x.toDouble()
                y = diagram.position.y.toDouble()
                width = diagram.dimension.width.toDouble()
                height = diagram.dimension.height.toDouble()
                bpmnShape.bounds = this
            }

        }

    }

}