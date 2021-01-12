package org.factscript.language.visualization.bpmn.model

import org.factscript.language.definition.Node
import org.factscript.language.impl.utils.*
import org.factscript.language.visualization.bpmn.diagram.*
import org.camunda.bpm.model.bpmn.*
import org.camunda.bpm.model.bpmn.instance.bpmndi.*
import org.camunda.bpm.model.bpmn.instance.di.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Association(val from: Element<out Node,*>, private val to: Element<out Node, *>, parent: Group<*>): Element<Node, org.camunda.bpm.model.bpmn.instance.Association>(to.node, parent) {

    override val elements: List<Element<*,*>> = listOf(Label(this.node, this))
    override val model: org.camunda.bpm.model.bpmn.instance.Association = process.model.newInstance(org.camunda.bpm.model.bpmn.instance.Association::class.java)
    override val diagram: Arrow = west.diagram.connect(east.diagram, via.diagram)

    init {
        process.paths.add(this)
    }

    override val west: Symbol<*, *> get() = from.east
    override val east: Symbol<*, *> get() = to.west

    val via: Group<*> get() = parent?.asType<Group<*>>()!!.let { if (it == from) it.exitGroup else it }

    override fun initDiagram() {
        throw UnsupportedOperationException()
    }

    override fun initModel() {

        with(model) {
            process.bpmnProcess.addChildElement(this)
            this.source = west.model
            this.target = east.model
            associationDirection = AssociationDirection.One
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

    }

}

