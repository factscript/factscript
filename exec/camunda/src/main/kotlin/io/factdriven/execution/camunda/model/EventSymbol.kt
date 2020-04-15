package io.factdriven.execution.camunda.model

import io.factdriven.definition.*
import io.factdriven.execution.Receptor
import org.camunda.bpm.model.bpmn.instance.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class EventSymbol<IN: Node, OUT: Event>(node: IN, parent: Element<out Flow,*>): Symbol<IN, OUT>(node, parent) {

    override val dimension: Dimension = Dimension(
        width = 36 + BpmnModel.margin.width * 2,
        height = 36 + BpmnModel.margin.height * 2
    )

    override fun position(child: Element<*, *>): Position {
        return Position(
            x = position.x,
            y = position.y + dimension.height - BpmnModel.margin.height + 6
        )
    }

    override fun init() {

        super.init()

        model.setAttributeValue("name", node.label.sentenceCase().replace(" ", "\n"), false)
        val messageEventDefinition = process.model.newInstance(MessageEventDefinition::class.java)
        model.addChildElement(messageEventDefinition)

    }

}

class CatchingEventSymbol(node: Catching, parent: Element<out Flow, *>): EventSymbol<Catching, CatchEvent>(node, parent) {

    override val model: CatchEvent = process.model.newInstance((if (node.isStart()) StartEvent::class else IntermediateCatchEvent::class).java)

    override fun init() {

        super.init()

        val message = process.model.definitions.getChildElementsByType(Message::class.java).find { it.id == node.id + "-Message" }
        if (message == null) {
            with(process.model.newInstance(Message::class.java)) {
                setAttributeValue("id", node.id + "-Message")
                val name = if (node.isStart()) { Receptor(node.type).hash } else "#{message}"
                setAttributeValue("name", name)
                process.model.definitions.addChildElement(this)
                model.getChildElementsByType(MessageEventDefinition::class.java).first().message = this
            }
        } else {
            model.getChildElementsByType(MessageEventDefinition::class.java).first().message = message
        }

    }

}

class ThrowingEventSymbol(node: Throwing, parent: Element<out Flow, *>): EventSymbol<Throwing, ThrowEvent>(node, parent) {

    override val model = process.model.newInstance((if (node.isFinish()) EndEvent::class else IntermediateThrowEvent::class).java)

}