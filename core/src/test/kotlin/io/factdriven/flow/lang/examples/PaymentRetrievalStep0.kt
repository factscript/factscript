package io.factdriven.flow.lang.examples

import io.factdriven.flow.*
import io.factdriven.flow.lang.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalStep0Test {

    @Test()
    fun execute() {

        val flow = execute <PaymentRetrieval> {
        }

        assertNotNull(flow)

    }

    @Test()
    fun on() {

        val flow = execute <PaymentRetrieval> {
            on
        }

        assertEquals(0, flow.elements.size)
    }

    @Test()
    fun onMessageType() {

        val flow = execute <PaymentRetrieval> {
            on message type(RetrievePayment::class)
        }

        val node = flow.elements.get(0)
        val pattern = (node as FlowMessageReactionDefinition).messagePattern

        assertTrue(node is FlowReactionDefinition)
        assertEquals(RetrievePayment::class, pattern.type)

    }

    @Test()
    fun create() {

        val flow = execute<PaymentRetrieval> {
            create
        }

        val node = flow.elements.get(0)

        assertTrue(node is FlowActionDefinition)

    }

    @Test()
    fun createSuccessWithoutAction() {

        val flow = execute<PaymentRetrieval> {
            create success("PaymentRetrieved")
        }

        val node = flow.elements.get(0) as FlowActionDefinition

        assertEquals(null, node.function)
    }

    @Test()
    fun createSuccessWithAction() {

        val flow = execute<PaymentRetrieval> {
            create success("PaymentRetrieved") by { PaymentRetrieved() }
        }

        val node = flow.elements.get(0) as FlowActionDefinition

        assertEquals(FlowActionType.Success, node.actionType)
        assertEquals(PaymentRetrieved::class, node.function!!.invoke(PaymentRetrieval(RetrievePayment()))::class)

    }

    @Test()
    fun executeService() {

        val flow = execute<PaymentRetrieval> {
            execute service {}
        }

        val node = flow.elements.get(0) as FlowDefinition

        assertEquals(FlowExecutionType.service, node.executionType)

    }

    @Test()
    fun executeServiceCreateIntent() {

        val flow = execute<PaymentRetrieval> {
            execute service {
                create intent("ChargeCreditCard") by { ChargeCreditCard() }
            }
        }

        val parentNode = flow.elements.get(0) as FlowDefinition
        val node = parentNode.elements.get(0)
        val action = node as FlowActionDefinition

        assertEquals(FlowActionType.Intent, action.actionType)
        assertEquals(ChargeCreditCard::class, action.function!!.invoke(PaymentRetrieval(RetrievePayment()))::class)
    }

    @Test()
    fun executeServiceOnMessage() {

        val flow = execute<PaymentRetrieval> {
            execute service {
                on message type(CreditCardCharged::class) create success("Credit card charged")
            }
        }

        val parentNode = flow.elements.get(0) as FlowDefinition
        val node = parentNode.elements.get(0) as FlowMessageReactionDefinition

        assertEquals(CreditCardCharged::class, node.messagePattern.type)
        assertEquals(FlowActionType.Success, node.actionType)
    }


    @Test()
    fun flow() {

        val flow = execute<PaymentRetrieval> {
            on message type(RetrievePayment::class)
            execute service {
                create intent("ChargeCreditCard") by { ChargeCreditCard() }
                on message type(CreditCardCharged::class)
            }
            create success ("PaymentRetrieved") by { PaymentRetrieved() }
        }

        val service = flow.elements[1] as FlowDefinition

        assertEquals(3, flow.elements.size)
        assertEquals(2, service.elements.size)

    }

}
