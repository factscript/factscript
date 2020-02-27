package io.factdriven.visualization.bpmn

import io.factdriven.execution.Handling
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.*
import org.camunda.bpm.model.bpmn.instance.bpmndi.*
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaExecutionListener
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaField
import org.camunda.bpm.model.bpmn.instance.dc.Bounds
import org.camunda.bpm.model.bpmn.instance.di.Waypoint
import kotlin.reflect.KClass


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

val zero = Position(
    160 - margin,
    80 - margin
)

abstract class BpmnSymbol(id: String, name: String, parent: Container): Symbol(id, name, parent) {

    abstract val elementClass: KClass<out FlowNode>

}

class BpmnTaskSymbol(id: String, name: String, parent: Container, val taskType: BpmnTaskType): BpmnSymbol(id, name, parent)  {

    override val inner = Dimension(100, 80)

    override val elementClass: KClass<out FlowNode> get() {
        return when (taskType) {
            BpmnTaskType.service -> ServiceTask::class
            BpmnTaskType.send -> SendTask::class
            BpmnTaskType.receive -> ReceiveTask::class
        }
    }

}

enum class BpmnTaskType {
    service, send, receive
}

enum class BpmnEventType {
    none, message
}

enum class BpmnEventCharacteristic {
    throwing, catching
}

enum class BpmnEventPosition {
    start, intermediate, end
}

class BpmnEventSymbol(id: String, name: String, parent: Container, val eventType: BpmnEventType, val characteristic: BpmnEventCharacteristic): BpmnSymbol(id, name, parent) {

    override val inner = Dimension(36, 36)

    override val elementClass: KClass<out FlowNode> get() {
        return when (characteristic) {
            BpmnEventCharacteristic.throwing -> {
                when (position()) {
                    BpmnEventPosition.intermediate -> IntermediateThrowEvent::class
                    BpmnEventPosition.end -> EndEvent::class
                    else -> throw IllegalStateException()
                }
            }
            BpmnEventCharacteristic.catching -> {
                when (position()) {
                    BpmnEventPosition.start -> StartEvent::class
                    BpmnEventPosition.intermediate -> IntermediateCatchEvent::class
                    else -> throw IllegalStateException()
                }
            }
        }
    }

    fun position(): BpmnEventPosition {
        return if (incoming.isEmpty()) {
            BpmnEventPosition.start
        } else if (outgoing.isEmpty()) {
            BpmnEventPosition.end
        } else {
            BpmnEventPosition.intermediate
        }
    }

}

fun transform(container: Container): BpmnModelInstance {

    val modelInstance = Bpmn.createEmptyModel()

    val definitions = modelInstance.newInstance(Definitions::class.java)
    val process = modelInstance.newInstance(Process::class.java)
    val plane = modelInstance.newInstance(BpmnPlane::class.java)
    val diagram = modelInstance.newInstance(BpmnDiagram::class.java)

    with(definitions) {
        targetNamespace = "https://factdriven.io/flowlang"
        modelInstance.definitions = this
    }

    with(process) {
        setAttributeValue("id", container.id, true)
        setAttributeValue("name", container.name.sentenceCase(), false)
        isExecutable = true
        definitions.addChildElement(this)
    }

    with(plane) {
        bpmnElement = process
    }

    with(diagram) {
        bpmnPlane = plane
        definitions.addChildElement(this)
    }


    val flowNodes = mutableMapOf<Symbol, FlowNode>()
    val messages = mutableMapOf<String, Message>()

    fun createBpmnModelElementInstance(symbol: BpmnSymbol): FlowNode {

        val modelElementInstance = modelInstance.newInstance(symbol.elementClass.java)
        process.addChildElement(modelElementInstance)

        modelElementInstance.setAttributeValue("id", symbol.id, false)

        val extensionElements = modelInstance.newInstance(ExtensionElements::class.java)
        modelElementInstance.addChildElement(extensionElements)

        with(extensionElements.addExtensionElement(CamundaExecutionListener::class.java)) {
            camundaClass = "io.factdriven.execution.camunda.CamundaFlowNodeStartListener"
            camundaEvent = "start"
        }

        when (symbol) {

            is BpmnEventSymbol -> {

                modelElementInstance.setAttributeValue("name", symbol.name.sentenceCase().replace(" ", "\n"), false)

                when (symbol.eventType) {

                    BpmnEventType.message -> {

                        val messageEventDefinition = modelInstance.newInstance(MessageEventDefinition::class.java)
                        modelElementInstance.addChildElement(messageEventDefinition)

                        if (symbol.characteristic == BpmnEventCharacteristic.catching) {

                            val message = messages[symbol.name]
                            if (message == null) {
                                with(modelInstance.newInstance(Message::class.java)) {
                                    setAttributeValue("id", symbol.name)
                                    setAttributeValue("name", if (symbol.position() == BpmnEventPosition.start) Handling(symbol.name).hash else "#{message}")
                                    definitions.addChildElement(this)
                                    messages[symbol.name] = this
                                    messageEventDefinition.message = this
                                }
                            } else {
                                messageEventDefinition.message = message
                            }

                        }

                    }
                }

            }

            is BpmnTaskSymbol -> {

                modelElementInstance.setAttributeValue("name", symbol.name.sentenceCase(), false)

                when (symbol.taskType) {

                    BpmnTaskType.receive -> {

                        val message = messages[symbol.name]
                        if (message == null) {
                            with(modelInstance.newInstance(Message::class.java)) {
                                setAttributeValue("id", symbol.name)
                                setAttributeValue("name", "#{message}")
                                definitions.addChildElement(this)
                                messages[symbol.name] = this
                                (modelElementInstance as ReceiveTask).message = this
                            }
                        } else {
                            (modelElementInstance as ReceiveTask).message = message
                        }

                    }

                    BpmnTaskType.send -> {

                        (modelElementInstance as SendTask).camundaExpression = "#{true}"

                    }

                    BpmnTaskType.service -> {

                        (modelElementInstance as ServiceTask).camundaType = "external"
                        modelElementInstance.camundaTopic = "#{message}"

                    }

                }

            }

        }

        val bpmnShape = modelInstance.newInstance(BpmnShape::class.java)
        bpmnShape.bpmnElement = modelElementInstance as BaseElement

        with(modelInstance.newInstance(Bounds::class.java)) {
            x = (symbol.topLeft.x + zero.x).toDouble()
            y = (symbol.topLeft.y + zero.y).toDouble()
            width = symbol.inner.width.toDouble()
            height = symbol.inner.height.toDouble()
            bpmnShape.bounds = this
        }

        val bpmnLabel = modelInstance.newInstance(BpmnLabel::class.java)
        bpmnShape.addChildElement(bpmnLabel)
        plane.addChildElement(bpmnShape)

        with(modelInstance.newInstance(Bounds::class.java)) {
            x = (symbol.topLeft.x + zero.x).toDouble() + 6
            y = (symbol.topLeft.y + zero.y).toDouble() + symbol.inner.height + 6
            height = 20.toDouble()
            width = symbol.inner.width.toDouble() - 12
            bpmnLabel.addChildElement(this)
        }

        return modelElementInstance

    }

    fun createSequenceFlow(connector: Connector) {

        val sequenceFlow = modelInstance.newInstance(SequenceFlow::class.java)

        val extensionElements = modelInstance.newInstance(ExtensionElements::class.java)

        with(extensionElements.addExtensionElement(CamundaExecutionListener::class.java)) {
            camundaClass = "io.factdriven.execution.camunda.CamundaFlowTransitionListener"
            camundaEvent = "take"
            val camundaField = modelInstance.newInstance(CamundaField::class.java)
            with(camundaField) {
                camundaName = "target"
                camundaStringValue = connector.target.id
            }
            addChildElement(camundaField)
        }

        with(sequenceFlow) {
            process.addChildElement(this)
            addChildElement(extensionElements)
            source = flowNodes[connector.source]
            source.outgoing.add(this)
            target = flowNodes[connector.target]
            target.incoming.add(this)
        }

        val bpmnEdge = modelInstance.newInstance(BpmnEdge::class.java)
        bpmnEdge.bpmnElement = sequenceFlow
        plane.addChildElement(bpmnEdge)

        connector.waypoints.forEach {
            with(modelInstance.newInstance(Waypoint::class.java)) {
                x = (it.x + zero.x).toDouble()
                y = (it.y + zero.y).toDouble()
                bpmnEdge.addChildElement(this)
            }
        }

    }

    container.symbols.forEach { flowNodes[it] = createBpmnModelElementInstance(it as BpmnSymbol) }
    container.connectors.forEach { createSequenceFlow(it) }

    return process.modelInstance as BpmnModelInstance

}

fun String.sentenceCase(): String = (replace("(.)([A-Z\\d])".toRegex()) { "${it.groupValues[1]} ${it.groupValues[2].toLowerCase()}" })
fun String.camelCase(): String = replace("\\s([a-z\\\\d])".toRegex()) { it.groupValues[1].toUpperCase() }