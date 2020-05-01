package io.factdriven.language.visualization.bpmn.model

import io.factdriven.execution.Receptor
import io.factdriven.language.definition.*
import io.factdriven.language.impl.utils.Id
import io.factdriven.language.impl.utils.asLines
import io.factdriven.language.impl.utils.construct
import io.factdriven.language.visualization.bpmn.diagram.Artefact
import io.factdriven.language.visualization.bpmn.diagram.Box
import io.factdriven.language.visualization.bpmn.diagram.Direction
import org.camunda.bpm.model.bpmn.instance.*
import org.joda.time.format.ISODateTimeFormat
import java.time.format.DateTimeFormatter

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
abstract class EventSymbol<IN: Node, OUT: Event>(node: IN, parent: Group<out Flow>): Symbol<IN, OUT>(node, parent) {

    override val diagram: Artefact = Artefact(36, 36, 18)

    override fun initModel() {

        super.initModel()

        model.setAttributeValue("name", node.description.asLines(), false)

    }

}

class CatchingEventSymbol(node: Catching, parent: Group<out Flow>): EventSymbol<Catching, CatchEvent>(node, parent) {

    override val model: CatchEvent = process.model.newInstance((if (node.isStart()) StartEvent::class else IntermediateCatchEvent::class).java)

    override fun initModel() {

        super.initModel()

        when (node) {
            is AwaitingTime -> {

                val timerEventDefinition = process.model.newInstance(TimerEventDefinition::class.java)
                model.addChildElement(timerEventDefinition)
                when(node.timer) {
                    Timer.Limit -> {
                        timerEventDefinition.timeDate = process.model.newInstance(TimeDate::class.java)
                        timerEventDefinition.timeDate.textContent = "#{${node.id.asBpmnId()}}"
                    }
                    Timer.Duration -> {
                        timerEventDefinition.timeDuration = process.model.newInstance(TimeDuration::class.java)
                        timerEventDefinition.timeDuration.textContent = "#{${node.id.asBpmnId()}}"
                    }
                    Timer.Cycle -> {
                        timerEventDefinition.timeCycle = process.model.newInstance(TimeCycle::class.java)
                        val timerDefinition = if (node.isStart()) {
                            val times = node.times!!.invoke(node.entity.construct())
                            val period = node.period!!.invoke(node.entity.construct())
                            "R$times/$period"
                        } else {
                            "#{${node.id.asBpmnId()}}"
                        }
                        timerEventDefinition.timeCycle.textContent = timerDefinition
                    }
                }

            }
            else -> {

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

class BoundaryEventSymbol(node: Catching, parent: Group<out Flow>): EventSymbol<Catching, BoundaryEvent>(node, parent) {

    override val model = process.model.newInstance(BoundaryEvent::class.java)
    override val diagram: Artefact = Artefact(36, 36, 36)

    override fun initDiagram() {
        super.initDiagram()
        diagram.attachTo(
            (parent.parent as Task).task.diagram as Box,
            if ((parent.parent as Task).sequences.indexOf(parent) < 2) Direction.South else Direction.North,
            if (((parent.parent as Task).sequences.indexOf(parent) % 2) == 1) Direction.East else Direction.West
        )
    }

    override fun initModel() {

        super.initModel()

        model.attachedTo = (parent.parent as Task).task.model
        model.cancelActivity()

        if (node is AwaitingTime) {

            val timerEventDefinition = process.model.newInstance(TimerEventDefinition::class.java)
            model.addChildElement(timerEventDefinition)
            when(node.timer) {
                Timer.Limit -> {
                    timerEventDefinition.timeDate = process.model.newInstance(TimeDate::class.java)
                    val timerDefinition = if (node.isStart()) {
                        val from = node.from!!.invoke(node.entity.construct())
                        val fmt: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        fmt.format(from)
                    } else {
                        "#{${node.id.asBpmnId()}}"
                    }
                    timerEventDefinition.timeDate.textContent = timerDefinition
                }
                Timer.Duration -> {
                    timerEventDefinition.timeDuration = process.model.newInstance(TimeDuration::class.java)
                    timerEventDefinition.timeDuration.textContent = "#{${node.id.asBpmnId()}}"
                }
                Timer.Cycle -> {
                    timerEventDefinition.timeCycle = process.model.newInstance(TimeCycle::class.java)
                    val timerDefinition = if (node.isStart()) {
                        val times = node.times!!.invoke(node.entity.construct())
                        val period = node.period!!.invoke(node.entity.construct())
                        "R$times/$period"
                    } else {
                        "#{${node.id.asBpmnId()}}"
                    }
                    timerEventDefinition.timeCycle.textContent = timerDefinition
                }
            }

        } else if (node is ConsumingEvent && node.isFailing()) {
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