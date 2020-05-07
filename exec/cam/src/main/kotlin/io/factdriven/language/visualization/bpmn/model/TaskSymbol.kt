package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.*
import io.factdriven.language.visualization.bpmn.diagram.Artefact
import org.camunda.bpm.model.bpmn.instance.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class TaskSymbol<IN: Node, OUT: org.camunda.bpm.model.bpmn.instance.Task>(node: IN, parent: Group<*>): Symbol<IN, OUT>(node, parent) {

    override val diagram: Artefact = Artefact(100, 80, 18)

    override fun initModel() {

        super.initModel()

        model.setAttributeValue("name", node.description, false)

    }

}

class ServiceTaskSymbol(node: Executing, parent: Group<*>): TaskSymbol<Executing, ServiceTask>(node, parent) {

    override val model = process.model.newInstance(ServiceTask::class.java)

    override fun initModel() {

        super.initModel()

        model.camundaType = "external"
        model.camundaTopic = "#{${node.id.asBpmnId()}}"

    }

}

class SendTaskSymbol(node: Throwing, parent: Group<*>): TaskSymbol<Throwing, SendTask>(node, parent) {

    override val model = process.model.newInstance(SendTask::class.java)

    override fun initModel() {

        super.initModel()

        model.camundaExpression = "#{true}"

    }

}

class ReceiveTaskSymbol(node: Consuming, parent: Group<*>): TaskSymbol<Consuming, ReceiveTask>(node, parent) {

    override val model = process.model.newInstance(ReceiveTask::class.java)

    override fun initModel() {

        super.initModel()

        val message = process.model.definitions.getChildElementsByType(Message::class.java).find { it.id == node.id + "-Message" }
            if (message == null) {
                with(process.model.newInstance(Message::class.java)) {
                    setAttributeValue("id", node.id + "-Message")
                    setAttributeValue("name", "#{${node.id.asBpmnId()}}")
                    process.model.definitions.addChildElement(this)
                    model.message = this
                }
            } else {
                model.message = message
            }

        }

}