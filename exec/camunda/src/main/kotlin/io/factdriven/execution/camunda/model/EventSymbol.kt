package io.factdriven.execution.camunda.model

import io.factdriven.definition.*
import io.factdriven.execution.Receptor
import io.factdriven.execution.camunda.diagram.Artefact
import io.factdriven.impl.utils.asLines
import org.camunda.bpm.model.bpmn.instance.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class EventSymbol<IN: Node, OUT: Event>(node: IN, parent: Group<out Flow>): Symbol<IN, OUT>(node, parent) {

    override val diagram: Artefact = Artefact(36, 36, 18)

    override fun initModel() {

        super.initModel()

        model.setAttributeValue("name", node.label.asLines(), false)
        val messageEventDefinition = process.model.newInstance(MessageEventDefinition::class.java)
        model.addChildElement(messageEventDefinition)

    }

}

class CatchingEventSymbol(node: Catching, parent: Group<out Flow>): EventSymbol<Catching, CatchEvent>(node, parent) {

    override val model: CatchEvent = process.model.newInstance((if (node.isStart()) StartEvent::class else IntermediateCatchEvent::class).java)

    override fun initModel() {

        super.initModel()

        val message = process.model.definitions.getChildElementsByType(Message::class.java).find { it.id == node.id + "-Message" }
        if (message == null) {
            with(process.model.newInstance(Message::class.java)) {
                setAttributeValue("id", node.id + "-Message")
                val name = if (node.isStart()) { Receptor(node.type).hash } else "#{${node.id.toMessageName()}}"
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

    override val model = process.model.newInstance((if (node.isFinish()) EndEvent::class else IntermediateThrowEvent::class).java)

}

fun String.toMessageName() = replace("-", "_")
fun String.fromMessageName() = replace("_", "-")