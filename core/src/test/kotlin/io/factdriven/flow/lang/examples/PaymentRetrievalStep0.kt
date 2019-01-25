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

        val flow = define <PaymentRetrieval> {
        }

        assertNotNull(flow)

    }

    @Test()
    fun on() {

        val flow = define <PaymentRetrieval> {
            on
        }

        assertEquals(0, flow.children.size)
    }

    @Test()
    fun onMessageType() {

        val flow = define <PaymentRetrieval> {
            on message RetrievePayment::class
        }

        val node = flow.children[0] as FlowMessageReactionDefinition

        assertEquals(RetrievePayment::class, node.messageType)

    }

    @Test()
    fun create() {

        val flow = define <PaymentRetrieval> {
            create
        }

        val node = flow.children.get(0)

        assertTrue(node is FlowActionDefinition)

    }

    @Test()
    fun createSuccessWithoutAction() {

        val flow = define <PaymentRetrieval> {
            create success "PaymentRetrieved"
        }

        val node = flow.children.get(0) as FlowActionDefinition

        assertEquals(null, node.function)
    }

    @Test()
    fun createSuccessWithAction() {

        val flow = define <PaymentRetrieval> {
            create success (PaymentRetrieved::class) by { PaymentRetrieved() }
        }

        val node = flow.children.get(0) as FlowActionDefinition

        assertEquals(FlowActionType.Success, node.actionType)
        assertEquals(PaymentRetrieved::class, node.function!!.invoke(PaymentRetrieval(RetrievePayment()))::class)

    }

    @Test()
    fun executeService() {

        val flow = define <PaymentRetrieval> {
            execute service {}
        }

        val node = flow.children.get(0) as FlowDefinition

        assertEquals(FlowExecutionType.service, node.executionType)

    }

    @Test()
    fun executeServiceCreateIntent() {

        val flow = define <PaymentRetrieval> {
            execute service {
                create intent ChargeCreditCard::class by { ChargeCreditCard() }
            }
        }

        val parentNode = flow.children.get(0) as FlowDefinition
        val node = parentNode.children.get(0)
        val action = node as FlowActionDefinition

        assertEquals(FlowActionType.Intent, action.actionType)
        assertEquals(ChargeCreditCard::class, action.function!!.invoke(PaymentRetrieval(RetrievePayment()))::class)
    }

    @Test()
    fun executeServiceOnMessage() {

        val flow = define <PaymentRetrieval> {
            execute service {
                on message CreditCardCharged::class create success("Credit card charged")
            }
        }

        val parentNode = flow.children.get(0) as FlowDefinition
        val node = parentNode.children.get(0) as FlowMessageReactionDefinition

        assertEquals(CreditCardCharged::class, node.messageType)
        assertEquals(FlowActionType.Success, node.action!!.actionType)
    }


    @Test()
    fun parentsAndElements() {

        val flow = define <PaymentRetrieval> {

            on message (RetrievePayment::class) create acceptance(PaymentRetrievalAccepted::class) by { PaymentRetrievalAccepted() }

            execute service {

                create intent ChargeCreditCard::class by { ChargeCreditCard() }
                on message (CreditCardCharged::class) having "reference" match { paymentId }

            }

            create success PaymentRetrieved::class by { PaymentRetrieved() }

        }

        assertEquals(3, flow.children.size)
        flow.children.forEach {
            assertEquals(flow, it.parent)
        }

        val service = flow.children[1] as FlowDefinition
        assertEquals(2, service.children.size)
        service.children.forEach {
            assertEquals(service, it.parent)
        }

    }

}
