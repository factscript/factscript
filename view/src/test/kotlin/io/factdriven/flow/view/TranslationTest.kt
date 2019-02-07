package io.factdriven.flow.view

import io.factdriven.flow.define
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class TranslationTest {

    @Test
    fun translateFlowExecution() {

        val flow = define <PaymentRetrieval> {
        }

        val element = translate(flow)

        assertNotNull(element.name)
        assertEquals("PaymentRetrieval", element.name)
        assertEquals(null, element.parent)
        assertEquals(Position(0, 0), element.position)
        assertEquals(Dimension(0, 0), element.dimension)

    }

    @Test
    fun translateFlowReaction() {

        val flow = define <PaymentRetrieval> {
            on message RetrievePayment::class create progress()
        }

        val element = translate(flow)

        assertNotNull(element.name)
        assertEquals("PaymentRetrieval", element.name)
        assertEquals(null, element.parent)
        assertEquals(Position(0, 0), element.position)
        assertEquals(Dimension(72, 72), element.dimension)
        assertEquals(1, element.children.size)

    }

    @Test
    fun translateFlowService() {

        val flow = define <PaymentRetrieval>("CustomPaymentRetrieval") {
            execute service {
                create intention ("ChargeCreditCard")
            }
        }

        val element = translate(flow)

        assertNotNull(element.name)
        assertEquals("CustomPaymentRetrieval", element.name)
        assertEquals(null, element.parent)
        assertEquals(Position(0, 0), element.position)
        assertEquals(Dimension(136, 116), element.dimension)
        assertEquals(1, element.children.size)

    }

    @Test
    fun translateFlowAction() {

        val flow = define <PaymentRetrieval>("CustomPaymentRetrieval") {
            create success ("PaymentRetrieved")
        }

        val element = translate(flow)

        assertNotNull(element.name)
        assertEquals("CustomPaymentRetrieval", element.name)
        assertEquals(null, element.parent)
        assertEquals(Position(0, 0), element.position)
        assertEquals(Dimension(72, 72), element.dimension)
        assertEquals(1, element.children.size)

    }

}