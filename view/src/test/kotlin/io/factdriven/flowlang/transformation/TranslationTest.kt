package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.execute
import org.junit.jupiter.api.Assertions.assertEquals
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

        val element = translate(flow)

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

        val element = translate(flow)

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

        val element = translate(flow)

        assertEquals("CustomPaymentRetrieval", element.id.key)
        assertEquals(null, element.parent)
        assertEquals(Position(0, 0), element.position)
        assertEquals(Dimension(72, 72), element.dimension)
        assertEquals("PaymentRetrieved", element.children[0].id.key)

    }

}