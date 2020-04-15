package io.factdriven.execution.camunda.model

import io.factdriven.definition.Conditional
import io.factdriven.definition.Node
import io.factdriven.execution.camunda.engine.CamundaFlowTransitionListener
import org.camunda.bpm.model.bpmn.instance.*
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener
import org.camunda.bpm.model.bpmn.instance.di.Waypoint

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Path(val from: Element<out Node,*>, val to: Element<out Node, *>, parent: Element<*,*>, val conditional: Conditional? = null): Element<Node, SequenceFlow>(to.node, parent) {

    private val wayPoints: List<Position> get() = from.wayPoints(this) + to.wayPoints(this)

    override val model: SequenceFlow = process.model.newInstance(SequenceFlow::class.java)

    override fun init() {

        val extensionElements = process.model.newInstance(ExtensionElements::class.java)

        with(extensionElements.addExtensionElement(CamundaExecutionListener::class.java)) {
            camundaClass = CamundaFlowTransitionListener::class.java.canonicalName
            camundaEvent = "take"
            val camundaField = modelInstance.newInstance(org.camunda.bpm.model.bpmn.instance.camunda.CamundaField::class.java)
            with(camundaField) {
                camundaName = "target"
                camundaStringValue = to.node.id
            }
            addChildElement(camundaField)
        }

        val source = (if (from is Symbol) from else from.children.last() as Symbol).model as FlowNode
        val target = (if (to is Symbol) to else to.children.first() as Symbol).model as FlowNode

        with(model) {
            process.bpmnProcess.addChildElement(this)
            addChildElement(extensionElements)
            this.source = source
            this.source.outgoing.add(this)
            this.target = target
            this.target.incoming.add(this)
        }

        val bpmnEdge = process.model.newInstance(BpmnEdge::class.java)
        bpmnEdge.bpmnElement = model
        process.bpmnPlane.addChildElement(bpmnEdge)

        wayPoints.forEach {
            with(process.model.newInstance(Waypoint::class.java)) {
                x = it.x.toDouble()
                y = it.y.toDouble()
                bpmnEdge.addChildElement(this)
            }
        }

        with(conditional) {

            if (this != null) {

                model.name = label.replace(" ", "\n")

                if (condition != null) {
                    model.conditionExpression = process.model.newInstance(org.camunda.bpm.model.bpmn.instance.ConditionExpression::class.java);
                    model.conditionExpression.textContent = "#{condition.evaluate(execution, '${id}')}"
                } else {
                    when (source) {
                        is ExclusiveGateway -> source.default = model
                        is InclusiveGateway -> source.default = model
                    }
                }

            }
        }

    }

    override val children: List<Element<*,*>> = if (conditional != null) listOf(Label(conditional, this)) else emptyList()

    override val dimension: Dimension get() = Dimension(wayPoints.maxBy { it.x }!!.x - wayPoints.minBy { it.x }!!.x, wayPoints.maxBy { it.y }!!.y - wayPoints.minBy { it.y }!!.y)

    override fun position(child: Element<*,*>): Position {
        return Position(parent!!.position.x, wayPoints[1].y - (conditional!!.label.toCharArray().filter { it == ' '  }.size) * 13 - 6) - BpmnModel.margin * 2 / 3
    }

    override fun entry(from: Direction): Position = wayPoints.first()

}

