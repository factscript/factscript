package org.factscript.language.visualization.bpmn.model

import org.factscript.language.*
import org.factscript.language.definition.*
import org.factscript.language.impl.definition.*
import org.factscript.language.impl.utils.asLines
import org.factscript.language.visualization.bpmn.diagram.Artefact
import org.factscript.language.visualization.bpmn.diagram.Box
import org.factscript.language.visualization.bpmn.diagram.Dimension
import org.factscript.language.visualization.bpmn.diagram.Direction
import org.camunda.bpm.model.bpmn.instance.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class EventSymbol<IN: Node, OUT: Event>(node: IN, parent: Group<out Flow>): Symbol<IN, OUT>(node, parent) {

    override val diagram: Artefact = Artefact(36, 36, 18)

    override fun initModel() {

        super.initModel()

        model.setAttributeValue("name", description.asLines(), false)

    }

}

class CatchingEventSymbol(node: Catching, parent: Group<out Flow>): EventSymbol<Catching, CatchEvent>(node, parent) {

    override val model: CatchEvent = process.model.newInstance((if (node.isStart()) StartEvent::class else IntermediateCatchEvent::class).java)

    override val elements: List<Element<*, *>> = super.elements + when(node) {
        is Waiting -> TimerEventSymbolDefinition(node, this)
        else -> MessageEventSymbolDefinition(node, this)
    }

}

class ThrowingEventSymbol(node: Throwing, parent: Group<out Flow>, val isCompensating: Boolean = false): EventSymbol<Throwing, ThrowEvent>(node, parent) {

    override val model = process.model.newInstance((if (node.isFinish() || (node.isLastSibling() && !node.isContinuing() && !isCompensating)) EndEvent::class else IntermediateThrowEvent::class).java)

    override val id: String get() = "${super.id}${if (isCompensating) "${positionSeparator}Compensate" else ""}"
    override val description: String get() = if (isCompensating) "" else node.description

    override val elements: List<Element<*, *>> = super.elements + if(node.isFailing()) {
        if (isCompensating) {
            CompensateEventSymbolDefinition(node, this)
        } else {
            val isFailure = Flows.all()
                .any { flow -> flow.descendants.filterIsInstance<Executing>()
                .any { executing -> executing.failureTypes.contains(node.throwing) } }
            if (isFailure) ErrorEventSymbolDefinition(node, this) else TerminateEventSymbolDefinition(node, this)
        }
    } else {
        MessageEventSymbolDefinition(node, this)
    }

}

class BoundaryEventSymbol(node: Catching, parent: Group<out Flow>): EventSymbol<Catching, BoundaryEvent>(node, parent) {

    override val model = process.model.newInstance(BoundaryEvent::class.java)
    override val diagram: Artefact = Artefact(Dimension(36, 36), Dimension(50, 0))

    override val elements: List<Element<*, *>> = super.elements + if (node is Waiting) {
        TimerEventSymbolDefinition(node, this)
    } else if (node is Consuming && Flows.find(reporting = node.consuming)?.find(Promising::class)?.failureTypes?.contains(node.consuming) == true) {
        if (node.promise.failureTypes.contains(node.consuming)) {
            CompensateEventSymbolDefinition(node, this)
        } else {
            ErrorEventSymbolDefinition(node, this)
        }
    } else {
        MessageEventSymbolDefinition(node, this)
    }

    override fun initModel() {

        super.initModel()

        model.attachedTo = (parent.parent as Task).task.model
        model.cancelActivity()

    }

    override fun initDiagram() {
        super.initDiagram()
        diagram.attachTo(
            (parent.parent as Task).task.diagram as Box,
            if ((parent.parent as Task).sequences.indexOf(parent) < 2) Direction.South else Direction.North,
            if (((parent.parent as Task).sequences.indexOf(parent) % 2) == 1) Direction.East else Direction.West
        )
    }

}

fun String.asBpmnId() = replace("-", "_")
fun String.fromBpmnId() = replace("_", "-")