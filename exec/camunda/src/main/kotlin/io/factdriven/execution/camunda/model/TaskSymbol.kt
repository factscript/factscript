package io.factdriven.execution.camunda.model

import io.factdriven.definition.*
import io.factdriven.execution.camunda.diagram.Artefact
import io.factdriven.execution.camunda.diagram.Dimension
import io.factdriven.execution.camunda.diagram.Position
import org.camunda.bpm.model.bpmn.instance.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class TaskSymbol<IN: Node, OUT: Task>(node: IN, parent: Group<out Flow>): Symbol<IN, OUT>(node, parent) {

    override val diagram: Artefact = Artefact(100, 80, 18)

    override fun initModel() {

        super.initModel()

        model.setAttributeValue("name", node.label, false)

    }

}

class ServiceTaskSymbol(node: Calling, parent: Group<out Flow>): TaskSymbol<Calling, ServiceTask>(node, parent) {

    override val model = process.model.newInstance(ServiceTask::class.java)

    override fun initModel() {

        super.initModel()

        model.camundaType = "external"
        model.camundaTopic = "#{${node.id.toMessageName()}}"

    }

}

class SendTaskSymbol(node: Throwing, parent: Group<out Flow>): TaskSymbol<Throwing, SendTask>(node, parent) {

    override val model = process.model.newInstance(SendTask::class.java)

    override fun initModel() {

        super.initModel()

        model.camundaExpression = "#{true}"

    }

}

class ReceiveTaskSymbol(node: Catching, parent: Group<out Flow>): TaskSymbol<Catching, ReceiveTask>(node, parent) {

    override val model = process.model.newInstance(ReceiveTask::class.java)

    override fun initModel() {

        super.initModel()

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