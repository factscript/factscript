package io.factdriven.flow.view

import io.factdriven.flow.execute
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class TranslationTest {

    @Test
    fun translateFlowExecution() {

        val flow = execute <PaymentRetrieval> {
        }

        val element = translate(flow)

        assertNotNull(element.elementType)
        assertEquals("PaymentRetrieval", element.elementType)
        assertEquals(null, element.parent)
        assertEquals(Position(0, 0), element.position)
        assertEquals(Dimension(0, 0), element.dimension)

    }

    @Test
    fun translateFlowReaction() {

        val flow = execute <PaymentRetrieval> {
            on message RetrievePayment::class create acceptance("")
        }

        val element = translate(flow)

        assertNotNull(element.elementType)
        assertEquals("PaymentRetrieval", element.elementType)
        assertEquals(null, element.parent)
        assertEquals(Position(0, 0), element.position)
        assertEquals(Dimension(72, 72), element.dimension)
        assertEquals(1, element.children.size)

    }

    @Test
    fun translateFlowService() {

        val flow = execute <PaymentRetrieval>("CustomPaymentRetrieval") {
            execute service {
                create intent ("ChargeCreditCard")
            }
        }

        val element = translate(flow)

        assertNotNull(element.elementType)
        assertEquals("CustomPaymentRetrieval", element.elementType)
        assertEquals(null, element.parent)
        assertEquals(Position(0, 0), element.position)
        assertEquals(Dimension(136, 116), element.dimension)
        assertEquals(1, element.children.size)

    }

    @Test
    fun translateFlowAction() {

        val flow = execute <PaymentRetrieval>("CustomPaymentRetrieval") {
            create success ("PaymentRetrieved")
        }

        val element = translate(flow)

        assertNotNull(element.elementType)
        assertEquals("CustomPaymentRetrieval", element.elementType)
        assertEquals(null, element.parent)
        assertEquals(Position(0, 0), element.position)
        assertEquals(Dimension(72, 72), element.dimension)
        assertEquals(1, element.children.size)

    }

}