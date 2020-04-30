package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.execution.Receptor
import io.factdriven.language.impl.utils.Id
import io.factdriven.language.visualization.bpmn.diagram.Artefact
import io.factdriven.language.impl.utils.asLines
import io.factdriven.language.visualization.bpmn.diagram.Attached
import io.factdriven.language.visualization.bpmn.diagram.Box
import org.camunda.bpm.model.bpmn.instance.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class EventSymbol<IN: Node, OUT: Event>(node: IN, parent: Group<out Flow>): Symbol<IN, OUT>(node, parent) {

    override val diagram: Artefact = Artefact(36, 36, 18)

    override fun initModel() {

        super.initModel()

        model.setAttributeValue("name", node.label.asLines(), false)

    }

}

class CatchingEventSymbol(node: Catching, parent: Group<out Flow>): EventSymbol<Catching, CatchEvent>(node, parent) {

    override val model: CatchEvent = process.model.newInstance((if (node.isStart()) StartEvent::class else IntermediateCatchEvent::class).java)

    override fun initModel() {

        super.initModel()

        val messageEventDefinition = process.model.newInstance(MessageEventDefinition::class.java)
        model.addChildElement(messageEventDefinition)

        val message = process.model.definitions.getChildElementsByType(Message::class.java).find { it.id == node.id + "-Message" }
        if (message == null) {
            with(process.model.newInstance(Message::class.java)) {
                setAttributeValue("id", node.id + "-Message")
                val name = if (node.isStart()) { Receptor(node.type).hash } else "#{${node.id.asBpmnId()}}"
                setAttributeValue("name", name)
                process.model.definitions.addChildElement(this)
                model.getChildElementsByType(MessageEventDefinition::class.java).first().message = this
            }
        } else {
            model.getChildElementsByType(MessageEventDefinition::class.java).first().message = message
        }

    }

}

class ThrowingEventSymbol(node: Throwing, parent: Group<out Flow>): EventSymbol<Throwing, ThrowEvent>(node, parent) {

    override val model = process.model.newInstance((if (node.isFinish() || !node.isContinuing()) EndEvent::class else IntermediateThrowEvent::class).java)

    override fun initModel() {

        super.initModel()

        if (node.isFailing()) {
            val errorEventDefinition = process.model.newInstance(ErrorEventDefinition::class.java)
            model.addChildElement(errorEventDefinition)
            val error = process.model.definitions.getChildElementsByType(Error::class.java).find { it.id == node.id + "-Error" }
            if (error == null) {
                with(process.model.newInstance(Error::class.java)) {
                    setAttributeValue("id", node.id + "-Error")
                    setAttributeValue("name", node.id.asBpmnId())
                    errorCode = Id(node.type)
                    process.model.definitions.addChildElement(this)
                    model.getChildElementsByType(ErrorEventDefinition::class.java).first().error = this
                }
            } else {
                model.getChildElementsByType(ErrorEventDefinition::class.java).first().error = error
            }
        } else {
            val messageEventDefinition = process.model.newInstance(MessageEventDefinition::class.java)
            model.addChildElement(messageEventDefinition)
        }

    }

}

class BoundaryEventSymbol(node: Awaiting, parent: Group<out Flow>): EventSymbol<Awaiting, BoundaryEvent>(node, parent) {

    override val model = process.model.newInstance(BoundaryEvent::class.java)
    override val diagram: Artefact = Artefact(36, 36, 36)

    override fun initDiagram() {
        super.initDiagram()
        diagram.attachTo((parent.parent as Task).task.diagram as Box)
    }

    override fun initModel() {

        super.initModel()

        model.attachedTo = (parent.parent as Task).task.model
        model.cancelActivity()

        if (node.isFailing()) {
            val errorEventDefinition = process.model.newInstance(ErrorEventDefinition::class.java)
            model.addChildElement(errorEventDefinition)
            val error = process.model.definitions.getChildElementsByType(Error::class.java).find { it.id == node.id + "-Error" }
            if (error == null) {
                with(process.model.newInstance(Error::class.java)) {
                    setAttributeValue("id", node.id + "-Error")
                    setAttributeValue("name", node.id.asBpmnId())
                    errorCode = Id(node.type)
                    process.model.definitions.addChildElement(this)
                    model.getChildElementsByType(ErrorEventDefinition::class.java).first().error = this
                }
            } else {
                model.getChildElementsByType(ErrorEventDefinition::class.java).first().error = error
            }
        } else {
            val messageEventDefinition = process.model.newInstance(MessageEventDefinition::class.java)
            model.addChildElement(messageEventDefinition)
            val message = process.model.definitions.getChildElementsByType(Message::class.java)
                .find { it.id == node.id + "-Message" }
            if (message == null) {
                with(process.model.newInstance(Message::class.java)) {
                    setAttributeValue("id", node.id + "-Message")
                    val name = if (node.isStart()) {
                        Receptor(node.type).hash
                    } else "#{${node.id.asBpmnId()}}"
                    setAttributeValue("name", name)
                    process.model.definitions.addChildElement(this)
                    model.getChildElementsByType(MessageEventDefinition::class.java).first().message = this
                }
            } else {
                model.getChildElementsByType(MessageEventDefinition::class.java).first().message = message
            }
        }

    }
}


fun String.asBpmnId() = replace("-", "_")
fun String.fromBpmnId() = replace("_", "-")