package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.language.visualization.bpmn.diagram.Container
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape
import org.camunda.bpm.model.bpmn.instance.dc.Bounds

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@Suppress("LeakingThis")
abstract class Group<IN: Node>(node: IN, parent: Element<*,*>): Element<IN, org.camunda.bpm.model.bpmn.instance.Group>(node, parent), Continuing {

    override val model = process.model.newInstance(org.camunda.bpm.model.bpmn.instance.Group::class.java)
    override val diagram = Container(36)

    abstract val exitConditional: ConditionalNode?
    internal open val exitGroup: Group<*> = this

    override fun initModel() {

        if (BpmnModel.renderGroups) {

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