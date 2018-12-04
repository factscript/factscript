package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.execute
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class GraphicalFlowNodeTest {

    private val whitespace = 18
    private val eventDimension = Dimension(36 + 2 * whitespace, 36 + 2* whitespace)
    private val taskDimension = Dimension(100 + 2* whitespace, 80 + 2* whitespace)

    @Test
    fun graphicalNoneStartEvent() {

        val graphicalNode = RenderedStartEvent("Retrieve payment")

        assertEquals(Position(0,0), graphicalNode.position)
        assertEquals(eventDimension, graphicalNode.dimension)

    }

    @Test
    fun graphicalServiceTask() {

        val graphicalNode = RenderedServiceTask("Charge credit card")

        assertEquals(Position(0,0), graphicalNode.position)
        assertEquals(taskDimension, graphicalNode.dimension)

    }

    @Test
    fun graphicalNoneEndEvent() {

        val graphicalNode = RenderedEndEvent("Payment retrieved")

        assertEquals(Position(0,0), graphicalNode.position)
        assertEquals(eventDimension, graphicalNode.dimension)

    }

    @Test
    fun graphicalFlowNodeSequence() {

        val sequence = GraphicalElementSequence()

        val startEvent = RenderedStartEvent("Retrieve payment")
        sequence.add(startEvent)
        val serviceTask = RenderedServiceTask("Charge credit card")
        sequence.add(serviceTask)
        val endEvent = RenderedEndEvent("Payment retrieved")
        sequence.add(endEvent)

        assertEquals(Position(0,0), sequence.position)
        assertEquals(Dimension(eventDimension.x * 2 + taskDimension.x, taskDimension.y), sequence.dimension)

        assertEquals(eventDimension, startEvent.dimension)
        assertEquals(taskDimension, serviceTask.dimension)
        assertEquals(eventDimension, endEvent.dimension)

        assertEquals(Position(0,(taskDimension.y - eventDimension.y) / 2), startEvent.position)
        assertEquals(Position(eventDimension.x, 0), serviceTask.position)
        assertEquals(Position(eventDimension.x + taskDimension.x, (taskDimension.y - eventDimension.y) / 2), endEvent.position)

    }

    @Test
    fun graphicalSequenceFlows() {
        val flow = execute <PaymentRetrieval> {
            on message type(RetrievePayment::class) create acceptance()
            execute service {
                create intent("Charge credit card") by { ChargeCreditCard() }
                on message type(CreditCardCharged::class) create success()
            }
            create success("Payment retrieved") by { PaymentRetrieved() }
        }
        val gFlow = translate(flow) as GraphicalElementSequence
        val rendered = gFlow.rendered()

        Assertions.assertEquals(5, rendered.size)

        val element1 = rendered[0] as RenderedEvent
        val element2 = rendered[1] as RenderedTask
        val element3 = rendered[2] as RenderedEvent
        val sequenceFlow1 = rendered[3] as RenderedSequenceFlow
        val sequenceFlow2 = rendered[4] as RenderedSequenceFlow

        Assertions.assertEquals(Position(18, 40), element1.renderingPosition)
        Assertions.assertEquals(Dimension(36, 36), element1.renderingDimension)

        Assertions.assertEquals(Position(90, 18), element2.renderingPosition)
        Assertions.assertEquals(Dimension(100, 80), element2.renderingDimension)

        Assertions.assertEquals(Position(226, 40), element3.renderingPosition)
        Assertions.assertEquals(Dimension(36, 36), element3.renderingDimension)

        Assertions.assertEquals(Position(54, 58), sequenceFlow1.from)
        Assertions.assertEquals(Position(90, 58), sequenceFlow1.to)
        Assertions.assertEquals(Position(190, 58), sequenceFlow2.from)
        Assertions.assertEquals(Position(226, 58), sequenceFlow2.to)

    }


}