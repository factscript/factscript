package io.factdriven.flow.view

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class BpmnRenderingTest {

    private val eventDimension = Dimension(
        36 + 2 * margin,
        36 + 2 * margin
    )
    private val taskDimension = Dimension(
        100 + 2 * margin,
        80 + 2 * margin
    )

    @Test
    fun bpmnStartEventSymbol() {

        val container = Sequence("PaymentRetrieval", "PaymentRetrieval")
        val symbol = BpmnEventSymbol(
            "RetrievePayment",
            "RetrievePayment",
            container,
            BpmnEventType.message,
            BpmnEventCharacteristic.catching
        )

        assertEquals(Position(0, 0), symbol.position)
        assertEquals(eventDimension, symbol.dimension)

    }

    @Test
    fun bpmnServiceTaskSymbol() {

        val container = Sequence("PaymentRetrieval", "PaymentRetrieval")
        val symbol = BpmnTaskSymbol(
            "ChargeCreditCard",
            "ChargeCreditCard",
            container,
            BpmnTaskType.service
        )

        assertEquals(Position(0, 0), symbol.position)
        assertEquals(taskDimension, symbol.dimension)

    }

    @Test
    fun bpmnEndEventSymbol() {

        val container = Sequence("PaymentRetrieval", "PaymentRetrieval")
        val symbol = BpmnEventSymbol(
            "PaymentRetrieved",
            "PaymentRetrieved",
            container,
            BpmnEventType.message,
            BpmnEventCharacteristic.throwing
        )

        assertEquals(Position(0, 0), symbol.position)
        assertEquals(eventDimension, symbol.dimension)

    }

    @Test
    fun sequence() {

        val container = Sequence("PaymentRetrieval", "PaymentRetrieval")

        val startEvent = BpmnEventSymbol(
            "RetrievePayment",
            "RetrievePayment",
            container,
            BpmnEventType.message,
            BpmnEventCharacteristic.catching
        )
        val serviceTask = BpmnTaskSymbol(
            "ChargeCreditCard",
            "ChargeCreditCard",
            container,
            BpmnTaskType.service
        )
        val endEvent = BpmnEventSymbol(
            "PaymentRetrieved",
            "PaymentRetrieved",
            container,
            BpmnEventType.message,
            BpmnEventCharacteristic.throwing
        )

        assertEquals(Position(0, 0), container.position)
        assertEquals(
            Dimension(
                eventDimension.width * 2 + taskDimension.width,
                taskDimension.height
            ), container.dimension)

        assertEquals(eventDimension, startEvent.dimension)
        assertEquals(taskDimension, serviceTask.dimension)
        assertEquals(eventDimension, endEvent.dimension)

        assertEquals(Position(0, (taskDimension.height - eventDimension.height) / 2), startEvent.position)
        assertEquals(Position(eventDimension.width, 0), serviceTask.position)
        assertEquals(
            Position(
                eventDimension.width + taskDimension.width,
                (taskDimension.height - eventDimension.height) / 2
            ), endEvent.position)

        val symbols = container.symbols
        val connectors = container.connectors

        assertEquals(3, symbols.size)
        assertEquals(2, connectors.size)

        val iteratorS = symbols.iterator()
        val symbol1 = iteratorS.next()
        val symbol2 = iteratorS.next()
        val symbol3 = iteratorS.next()

        assertEquals(Position(x = 0, y = 22), symbol1.position)
        assertEquals(Position(x = 18, y = 40), symbol1.topLeft)
        assertEquals(Dimension(width = 36, height = 36), symbol1.inner)

        assertEquals(Position(x = 72, y = 0), symbol2.position)
        assertEquals(Position(x = 90, y = 18), symbol2.topLeft)
        assertEquals(Dimension(width = 100, height = 80), symbol2.inner)

        assertEquals(Position(x = 208, y = 22), symbol3.position)
        assertEquals(Position(x = 226, y = 40), symbol3.topLeft)
        assertEquals(Dimension(width = 36, height = 36), symbol3.inner)

        val iteratorC = connectors.iterator()
        val connector1 = iteratorC.next()
        val connector2 = iteratorC.next()

        assertEquals(listOf(
            Position(x = 54, y = 58),
            Position(x = 90, y = 58)
        ), connector1.waypoints)
        assertEquals(listOf(
            Position(x = 190, y = 58),
            Position(x = 226, y = 58)
        ), connector2.waypoints)

    }

}