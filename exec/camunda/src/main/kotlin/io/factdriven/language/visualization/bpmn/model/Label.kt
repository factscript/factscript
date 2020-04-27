package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.Conditional
import io.factdriven.language.definition.Node
import io.factdriven.language.visualization.bpmn.diagram.*
import io.factdriven.language.impl.utils.toLines
import org.camunda.bpm.model.bpmn.instance.BaseElement
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnLabel
import org.camunda.bpm.model.bpmn.instance.dc.Bounds

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Label(node: Node, parent: Element<*, out BaseElement>): Element<Node, BpmnLabel>(node, parent) {

    override val model = process.model.newInstance(BpmnLabel::class.java)
    override val diagram: Artefact = Artefact(20,20,0)
    override val elements = emptyList<Element<*,*>>()

    override val east: Symbol<*, *> get() = TODO("Not yet implemented")
    override val west: Symbol<*, *> get() = TODO("Not yet implemented")

    override fun initDiagram() {
        // Nothing to initialize
    }

    override fun initModel() {

        val position = if (parent is Path && parent.conditional != null) when {
            parent.from is Loop || parent.from is Sequence -> (parent.from.diagram as Box).position + (parent.from.diagram as Box).east west 12 north 17
            parent.parent?.parent is Loop -> (parent.parent?.parent as Loop).fork.diagram.raw.position + (parent.parent?.parent as Loop).fork.diagram.raw.north east 10 north 17
            else -> Position(
                (parent.parent!!.diagram as Box).position.x,
                parent.diagram.waypoints[1].y
            ) west 12 north ((parent.conditional!!.label.toLines().size - 1) * 13 + 20)
        } else when {
            parent is EventSymbol -> parent.diagram.raw.position + parent.diagram.raw.south west parent.diagram.raw.dimension.width / 2 east 7 south 6
            parent is TaskSymbol -> parent.diagram.raw.position west 6 south 6
            parent is GatewaySymbol && parent.parent is Branch && (parent.parent as Branch).needsSouthLabel() -> parent.diagram.raw.position south parent.diagram.raw.dimension.height south 6 east 14
            parent is GatewaySymbol && parent.parent is Branch && (parent.parent as Branch).needsNorthWestLabel() -> parent.diagram.raw.position north ((node.label.toLines().size - 1) * 13 + 18) west (node.label.toLines().maxBy { it.length }!!.length * 3) east 14
            parent is GatewaySymbol && parent.parent is Branch -> parent.diagram.raw.position north ((node.label.toLines().size - 1) * 13 + 18) east 14
            parent is GatewaySymbol && parent.parent is Loop -> parent.diagram.raw.position south parent.diagram.raw.dimension.height south 6 east 14
            else -> null
        }

        if (position != null) {
            (parent!!.model as BaseElement).diagramElement.addChildElement(model)
            process.bpmnProcess.diagramElement.addChildElement((parent.model as BaseElement).diagramElement)
            with(process.model.newInstance(Bounds::class.java)) {
                x = position.x.toDouble()
                y = position.y.toDouble()
                height = diagram.raw.dimension.height.toDouble()
                width = diagram.raw.dimension.width.toDouble()
                model.addChildElement(this)
            }
        }

    }

}

fun Branch.needsAdaptedLabel(): Boolean {
    val hasDefaultFlow = branches.find {
        it.node.children.isEmpty() ||
                (it.node.children.first() is Conditional
                        && (it.node.children.first() as Conditional).condition == null )
    } != null
    val defaultFlowIsFirst = hasDefaultFlow && branches.first().node.children.first() is Conditional && (branches.first().node.children.first() as Conditional).condition == null
    return hasDefaultFlow && !defaultFlowIsFirst
}

fun Branch.needsSouthLabel(): Boolean {
    return needsAdaptedLabel() && branches.size <= 2
}

fun Branch.needsNorthWestLabel(): Boolean {
    return needsAdaptedLabel() && branches.size > 2
}
