package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.execute
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class TranslationTest {

    @Test
    fun translateFlowExecution() {

        val flow = execute <PaymentRetrieval> {
        }

        val element = transform2(flow)

        assertEquals("PaymentRetrieval", element.id.key)
        assertEquals("Payment retrieval", element.id.label)
        assertEquals(null, element.parent)
        assertEquals(Position(0, 0), element.position)
        assertEquals(Dimension(0, 0), element.dimension)

    }

    @Test
    fun translateFlowReaction() {

        val flow = execute <PaymentRetrieval> {
            on message type(RetrievePayment::class) create acceptance()
        }

        val element = transform2(flow)

        assertEquals("PaymentRetrieval", element.id.key)
        assertEquals(null, element.parent)
        assertEquals(Position(0, 0), element.position)
        assertEquals(Dimension(72, 72), element.dimension)
        assertEquals("RetrievePayment", element.children[0].id.key)

    }

    @Test
    fun translateFlowService() {

        val flow = execute <PaymentRetrieval> ("CustomPaymentRetrieval") {
            execute service {
                create intent("ChargeCreditCard")
            }
        }

        val element = transform2(flow)

        assertEquals("CustomPaymentRetrieval", element.id.key)
        assertEquals(null, element.parent)
        assertEquals(Position(0, 0), element.position)
        assertEquals(Dimension(136, 116), element.dimension)
        assertEquals("ChargeCreditCard", element.children[0].id.key)

    }

    @Test
    fun translateFlowAction() {

        val flow = execute <PaymentRetrieval> ("CustomPaymentRetrieval")  {
            create success("PaymentRetrieved")
        }

        val element = transform2(flow)

        assertEquals("CustomPaymentRetrieval", element.id.key)
        assertEquals(null, element.parent)
        assertEquals(Position(0, 0), element.position)
        assertEquals(Dimension(72, 72), element.dimension)
        assertEquals("PaymentRetrieved", element.children[0].id.key)

    }

    @Test
    fun translateFlow() {

        val flow = execute <PaymentRetrieval> {
            on message type(RetrievePayment::class) create acceptance()
            execute service {
                create intent("ChargeCreditCard") by { ChargeCreditCard() }
                on message type(CreditCardCharged::class) create success()
            }
            create success("PaymentRetrieved") by { PaymentRetrieved() }
        }

        val container = transform2(flow)
        val symbols = container.symbols
        val connectors = container.connectors

        assertEquals(3, symbols.size)
        assertEquals(2, connectors.size)

        val iteratorS = symbols.iterator()
        val symbol1 = iteratorS.next()
        val symbol2 = iteratorS.next()
        val symbol3 = iteratorS.next()

        assertEquals(Position(x=0, y=22), symbol1.position)
        assertEquals(Position(x=18, y=40), symbol1.topLeft)
        assertEquals(Dimension(width=36, height=36), symbol1.inner)

        assertEquals(Position(x=72, y=0), symbol2.position)
        assertEquals(Position(x=90, y=18), symbol2.topLeft)
        assertEquals(Dimension(width=100, height=80), symbol2.inner)

        assertEquals(Position(x=208, y=22), symbol3.position)
        assertEquals(Position(x=226, y=40), symbol3.topLeft)
        assertEquals(Dimension(width=36, height=36), symbol3.inner)

        val iteratorC = connectors.iterator()
        val connector1 = iteratorC.next()
        val connector2 = iteratorC.next()

        assertEquals(listOf(Position(x=54,y=58), Position(x=90,y=58)), connector1.waypoints)
        assertEquals(listOf(Position(x=190, y=58), Position(x=226, y=58)), connector2.waypoints)

    }

}