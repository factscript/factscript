package io.factdriven.execution.camunda.model

import io.factdriven.definition.*
import org.camunda.bpm.model.bpmn.instance.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class TaskSymbol<IN: Node, OUT: Task>(node: IN, parent: Element<out Flow, *>): Symbol<IN, OUT>(node, parent) {

    override val dimension: Dimension = Dimension(
        width = 100 + BpmnModel.margin.width * 2,
        height = 80 + BpmnModel.margin.height * 2
    )

    override fun position(child: Element<*, *>): Position {
        return Position(
            x = position.x + BpmnModel.margin.height + 6,
            y = position.y + BpmnModel.margin.height + 6
        )
    }

    override fun init() {

        super.init()

        model.setAttributeValue("name", node.label.sentenceCase(), false)

    }

}

class ServiceTaskSymbol(node: Executing, parent: Element<out Flow, *>): TaskSymbol<Executing, ServiceTask>(node, parent) {

    override val model = process.model.newInstance(ServiceTask::class.java)

    override fun init() {

        super.init()

        model.camundaType = "external"
        model.camundaTopic = "#{${node.id.toMessageName()}}"

    }

}

class SendTaskSymbol(node: Throwing, parent: Element<out Flow, *>): TaskSymbol<Throwing, SendTask>(node, parent) {

    override val model = process.model.newInstance(SendTask::class.java)

    override fun init() {

        super.init()

        model.camundaExpression = "#{true}"

    }

}

class ReceiveTaskSymbol(node: Catching, parent: Element<out Flow, *>): TaskSymbol<Catching, ReceiveTask>(node, parent) {

    override val model = process.model.newInstance(ReceiveTask::class.java)

    override fun init() {

        super.init()

        val message = process.model.definitions.getChildElementsByType(Message::class.java).find { it.id == node.id + "-Message" }
            if (message == null) {
                with(process.model.newInstance(Message::class.java)) {
                    setAttributeValue("id", node.id + "-Message")
                    setAttributeValue("name", "#{${node.id.toMessageName()}}")
                    process.model.definitions.addChildElement(this)
                    model.message = this
                }
            } else {
                model.message = message
            }

        }

}