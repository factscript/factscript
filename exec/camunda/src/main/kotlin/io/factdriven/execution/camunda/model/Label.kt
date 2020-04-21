package io.factdriven.execution.camunda.model

import io.factdriven.definition.Node
import io.factdriven.execution.camunda.diagram.Dimension
import io.factdriven.execution.camunda.diagram.Direction
import io.factdriven.execution.camunda.diagram.Position
import org.camunda.bpm.model.bpmn.instance.BaseElement
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnLabel
import org.camunda.bpm.model.bpmn.instance.dc.Bounds
import java.lang.IllegalStateException
import java.lang.UnsupportedOperationException

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Label(node: Node, parent: Element<*, out BaseElement>): Element<Node, BpmnLabel>(node, parent) {

    override val model = process.model.newInstance(BpmnLabel::class.java)

    override val children = emptyList<Element<*,*>>()
    override lateinit var dimension: Dimension
    override fun position(child: Element<*,*>): Position { throw IllegalStateException() }

    override fun init() {

        (parent!!.model as BaseElement).diagramElement.addChildElement(model)
        process.bpmnProcess.diagramElement.addChildElement((parent.model as BaseElement).diagramElement)

        with(process.model.newInstance(Bounds::class.java)) {
            x = position.x.toDouble()
            y = position.y.toDouble()
            height = 20.toDouble()
            width = if (parent is Path) 20.toDouble() else parent.dimension.width.toDouble()
            model.addChildElement(this)
        }

    }

    override fun entry(from: Direction): Position {
        throw UnsupportedOperationException()
    }

}