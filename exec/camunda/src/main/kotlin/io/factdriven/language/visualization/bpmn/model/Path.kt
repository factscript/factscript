package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.Conditional
import io.factdriven.language.definition.Looping
import io.factdriven.language.definition.Node
import io.factdriven.language.visualization.bpmn.diagram.*
import io.factdriven.language.execution.camunda.CamundaFlowTransitionListener
import io.factdriven.language.impl.utils.asLines
import org.camunda.bpm.model.bpmn.instance.*
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener
import org.camunda.bpm.model.bpmn.instance.di.Waypoint

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Path(val from: Element<out Node,*>, val to: Element<out Node, *>, parent: Element<*,*>, val conditional: Conditional? = null, val via: Container? = null): Element<Node, SequenceFlow>(to.node, parent) {

    override val model: SequenceFlow = process.model.newInstance(SequenceFlow::class.java)
    override val diagram: Arrow = (from.diagram as Box).connect(to.diagram as Box, via ?: parent.diagram as Box).arrows.last()

    override fun initDiagram() {
        //
    }

    override fun initModel() {

        val extensionElements = process.model.newInstance(ExtensionElements::class.java)

        fun symbol(element: Element<*,*>, first: Boolean): Symbol<*,*> {
            return (if (element is Symbol) element else symbol(if (first) element.children.first() else element.children.last(), first))
        }

        val source = symbol(from, false).model
        val target = symbol(to, true).model

        with(extensionElements.addExtensionElement(CamundaExecutionListener::class.java)) {
            camundaClass = CamundaFlowTransitionListener::class.java.canonicalName
            camundaEvent = "take"
            val camundaField = modelInstance.newInstance(org.camunda.bpm.model.bpmn.instance.camunda.CamundaField::class.java)
            with(camundaField) {
                camundaName = "target"
                camundaStringValue = symbol(to, true).node.id
            }
            addChildElement(camundaField)
        }

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

        diagram.waypoints.forEach {
            with(process.model.newInstance(Waypoint::class.java)) {
                x = it.x.toDouble()
                y = it.y.toDouble()
                bpmnEdge.addChildElement(this)
            }
        }

        with(conditional) {

            if (this != null) {

                model.name = if (parent is Looping) (if (condition != null) "Yes" else "No") else label.asLines()

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

}

