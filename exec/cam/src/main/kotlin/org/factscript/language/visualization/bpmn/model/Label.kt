package org.factscript.language.visualization.bpmn.model

import org.factscript.language.definition.Conditional
import org.factscript.language.definition.Looping
import org.factscript.language.definition.Node
import org.factscript.language.impl.utils.asType
import org.factscript.language.impl.utils.toLines
import org.factscript.language.visualization.bpmn.diagram.Artefact
import org.factscript.language.visualization.bpmn.diagram.Box
import org.factscript.language.visualization.bpmn.diagram.Position
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

    override val east: Symbol<*, *> get() = throw UnsupportedOperationException()
    override val west: Symbol<*, *> get() = throw UnsupportedOperationException()

    override fun initDiagram() {
        // Nothing to initialize
    }

    override fun initModel() {

        val position = if (parent is Path && parent.conditional != null) when {

            parent.from.asType<Group<*>>()?.exitConditional?.parent is Looping -> (parent.from.diagram as Box).position + (parent.from.diagram as Box).east west 12 north 20
            parent.parent?.parent is Loop -> (parent.parent?.parent as Loop).fork.diagram.raw.position + (parent.parent?.parent as Loop).fork.diagram.raw.north east 11 north 19

            else -> Position(parent.via.diagram.position.x, parent.diagram.waypoints[1].y) west 12 north ((parent.conditional.description.toLines().size - 1) * 13 + 20)

        } else when {

            parent is BoundaryEventSymbol && parent.labelIndex() == 0 && parent.numberOfBoundaries() > 1 -> parent.diagram.raw.position + parent.diagram.raw.south east 6 south 18 east ((node.description.toLines().maxBy { it.length }!!.length / 2 + 3) * 5) north ((node.description.toLines().size - 1) * 13 + 18)
            parent is BoundaryEventSymbol && parent.labelIndex() == 0 -> parent.diagram.raw.position + parent.diagram.raw.south west parent.diagram.raw.dimension.width / 2 east 10 south 3 west ((node.description.toLines().maxBy { it.length }!!.length / 2 + 3) * 5)
            parent is BoundaryEventSymbol && parent.labelIndex() == 1 -> parent.diagram.raw.position + parent.diagram.raw.south west parent.diagram.raw.dimension.width / 2 east 10 south 3 west ((node.description.toLines().maxBy { it.length }!!.length / 2 + 3) * 5)
            parent is BoundaryEventSymbol && parent.labelIndex() == 2 -> parent.diagram.raw.position + parent.diagram.raw.north west parent.diagram.raw.dimension.width / 2 east 10 south 3 west ((node.description.toLines().maxBy { it.length }!!.length / 2 + 3) * 5) north ((node.description.toLines().size - 1) * 13 + 18)
            parent is EventSymbol -> parent.diagram.raw.position + parent.diagram.raw.south west parent.diagram.raw.dimension.width / 2 east 7 south 6
            parent is TaskSymbol -> parent.diagram.raw.position west 6 south 6
            parent is GatewaySymbol && parent.parent is Branch && (parent.parent as Branch).needsSouthLabel() -> parent.diagram.raw.position south parent.diagram.raw.dimension.height south 6 east 14
            parent is GatewaySymbol && parent.parent is Branch && (parent.parent as Branch).needsNorthWestLabel() -> parent.diagram.raw.position north ((node.description.toLines().size - 1) * 13 + 18) west ((node.description.toLines().maxBy { it.length }!!.length  / 2 + 1) * 6) east 14
            parent is GatewaySymbol && parent.parent is Branch -> parent.diagram.raw.position north ((node.description.toLines().size - 1) * 13 + 18) east 14
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

fun BoundaryEventSymbol.labelIndex(): Int {
    return (parent.parent as? Task)?.sequences?.indexOf(parent) ?: 0
}

fun BoundaryEventSymbol.numberOfBoundaries(): Int {
    return (parent.parent as? Task)?.sequences?.size ?: 0
}

fun Branch.needsAdaptedLabel(): Boolean {
    val hasDefaultFlow = sequences.find {
        it.node.children.isEmpty() ||
                (it.node.children.first() is Conditional
                        && (it.node.children.first() as Conditional).condition == null )
    } != null
    val defaultFlowIsFirst = hasDefaultFlow && sequences.first().node.children.first() is Conditional && (sequences.first().node.children.first() as Conditional).condition == null
    return hasDefaultFlow && !defaultFlowIsFirst
}

fun Branch.needsSouthLabel(): Boolean {
    return needsAdaptedLabel() && sequences.size <= 2
}

fun Branch.needsNorthWestLabel(): Boolean {
    return needsAdaptedLabel() && sequences.size > 2
}
