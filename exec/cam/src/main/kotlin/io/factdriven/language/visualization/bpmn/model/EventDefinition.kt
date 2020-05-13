package io.factdriven.language.visualization.bpmn.model

import io.factdriven.execution.*
import io.factdriven.language.definition.*
import io.factdriven.language.impl.utils.*
import org.camunda.bpm.model.bpmn.instance.*
import org.camunda.bpm.model.bpmn.instance.EventDefinition
import org.camunda.bpm.model.bpmn.instance.Message

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class EventSymbolDefinition<IN: Node, OUT: EventDefinition>(node: IN, parent: EventSymbol<*, *>): Element<IN, OUT>(node, parent) {

    override val east: Symbol<*, *> get() = parent!!.east
    override val west: Symbol<*, *> get() = parent!!.west

    override val elements: List<Element<*, *>> = emptyList()

    override val diagram: Any get() = throw UnsupportedOperationException()
    override fun initDiagram() { /* Nothing to display here */ }

    override fun initModel() {
        (parent?.model as Event).addChildElement(model)
    }

}

class MessageEventSymbolDefinition(node: Node, parent: EventSymbol<*, *>): EventSymbolDefinition<Node, MessageEventDefinition>(node, parent) {

    override val model = process.model.newInstance(MessageEventDefinition::class.java)

    override fun initModel() {

        super.initModel()

        if (node is Consuming) {
            model.message = process.model.definitions.getChildElementsByType(Message::class.java).find { it.id == node.id + "-Message" }
                ?: with(process.model.newInstance(Message::class.java)) {
                    setAttributeValue("id", node.id + "-Message")
                    val name = if (node.isStart()) {
                        Receptor(node.type).hash
                    } else "#{${node.id.asBpmnId()}}"
                    setAttributeValue("name", name)
                    process.model.definitions.addChildElement(this)
                    return@with this
                }
        }

    }

}

class TimerEventSymbolDefinition(node: Waiting, parent: EventSymbol<*, *>): EventSymbolDefinition<Waiting, TimerEventDefinition>(node, parent) {

    override val model = process.model.newInstance(TimerEventDefinition::class.java)

    override fun initModel() {

        super.initModel()

        when(node.timer) {
            Timer.Limit -> {
                model.timeDate = process.model.newInstance(TimeDate::class.java)
                model.timeDate.textContent = "#{${node.id.asBpmnId()}}"
            }
            Timer.Duration -> {
                model.timeDuration = process.model.newInstance(TimeDuration::class.java)
                model.timeDuration.textContent = "#{${node.id.asBpmnId()}}"
            }
        }

    }

}

class ErrorEventSymbolDefinition(node: Node, parent: EventSymbol<*, *>): EventSymbolDefinition<Node, ErrorEventDefinition>(node, parent) {

    override val model: ErrorEventDefinition = process.model.newInstance(ErrorEventDefinition::class.java)

    override fun initModel() {

        super.initModel()

        model.error = process.model.definitions.getChildElementsByType(Error::class.java).find { it.id == node.id + "-Error" }
            ?: with(process.model.newInstance(Error::class.java)) {
                setAttributeValue("id", node.id + "-Error")
                setAttributeValue("name", node.id.asBpmnId())
                errorCode = Id(node.type)
                process.model.definitions.addChildElement(this)
                return@with this
            }

    }

}

class CompensateEventSymbolDefinition(node: Node, parent: EventSymbol<*, *>): EventSymbolDefinition<Node, CompensateEventDefinition>(node, parent) {

    override val model = process.model.newInstance(CompensateEventDefinition::class.java)

    override fun initModel() {

        super.initModel()

        if (node is Throwing)
            model.isWaitForCompletion = true

    }

}
