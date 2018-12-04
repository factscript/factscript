package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.execute
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class FlowNodeTranslationTest {

    @Test
    fun translateFlowExecution() {
        val flow = execute <PaymentRetrieval> {
        }
        val gFlow = translate(flow)
        assertEquals("PaymentRetrieval", gFlow.label)
        assertEquals(null, gFlow.parent)
        assertEquals(Position(0,0), gFlow.position)
        assertEquals(Dimension(0,0), gFlow.dimension)
    }

    @Test
    fun translateFlowReaction() {
        val flow = execute <PaymentRetrieval> {
            on message type(RetrievePayment::class) create acceptance()
        }
        val gFlow = translate(flow)
        assertEquals("PaymentRetrieval", gFlow.label)
        assertEquals(null, gFlow.parent)
        assertEquals(Position(0,0), gFlow.position)
        assertEquals(Dimension(72,72), gFlow.dimension)
        val gElement = (gFlow as GraphicalElementSequence).children[0]
        assertEquals("RetrievePayment", gElement.label)
    }

    @Test
    fun translateFlowService() {
        val flow = execute <PaymentRetrieval> ("Custom payment retrieval")  {
            execute service {
                create intent("Charge credit card") by {}
            }
        }
        val gFlow = translate(flow)
        assertEquals("Custom payment retrieval", gFlow.label)
        assertEquals(null, gFlow.parent)
        assertEquals(Position(0,0), gFlow.position)
        assertEquals(Dimension(136,116), gFlow.dimension)
        val gElement = (gFlow as GraphicalElementSequence).children[0]
        assertEquals("Charge credit card", gElement.label)
    }

    @Test
    fun translateFlowAction() {
        val flow = execute <PaymentRetrieval> ("Custom payment retrieval")  {
            create success("Payment retrieved") by {}
        }
        val gFlow = translate(flow)
        assertEquals("Custom payment retrieval", gFlow.label)
        assertEquals(null, gFlow.parent)
        assertEquals(Position(0,0), gFlow.position)
        assertEquals(Dimension(72,72), gFlow.dimension)
        val gElement = (gFlow as GraphicalElementSequence).children[0]
        assertEquals("Payment retrieved", gElement.label)
    }

    @Test
    fun translateFlow() {
        val flow = execute <PaymentRetrieval> {
            on message type(RetrievePayment::class) create acceptance()
            execute service {
                create intent("Charge credit card") by { ChargeCreditCard() }
                on message type(CreditCardCharged::class) create success()
            }
            create success("Payment retrieved") by { PaymentRetrieved() }
        }
        val gFlow = translate(flow) as GraphicalElementSequence
        assertEquals(3, gFlow.children.size)
    }

}