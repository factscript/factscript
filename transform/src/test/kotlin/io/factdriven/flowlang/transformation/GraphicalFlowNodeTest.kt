package io.factdriven.flowlang.transformation

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

        val graphicalNode = GraphicalNoneStartEvent("Retrieve payment")

        assertEquals(Position(0,0), graphicalNode.position)
        assertEquals(eventDimension, graphicalNode.dimension)

    }

    @Test
    fun graphicalServiceTask() {

        val graphicalNode = GraphicalServiceTask("Charge credit card")

        assertEquals(Position(0,0), graphicalNode.position)
        assertEquals(taskDimension, graphicalNode.dimension)

    }

    @Test
    fun graphicalNoneEndEvent() {

        val graphicalNode = GraphicalNoneEndEvent("Payment retrieved")

        assertEquals(Position(0,0), graphicalNode.position)
        assertEquals(eventDimension, graphicalNode.dimension)

    }

    @Test
    fun graphicalFlowNodeSequence() {

        val sequence = GraphicalFlowNodeSequence()

        val startEvent = GraphicalNoneStartEvent("Retrieve payment")
        sequence.add(startEvent)
        val serviceTask = GraphicalServiceTask("Charge credit card")
        sequence.add(serviceTask)
        val endEvent = GraphicalNoneEndEvent("Payment retrieved")
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

}