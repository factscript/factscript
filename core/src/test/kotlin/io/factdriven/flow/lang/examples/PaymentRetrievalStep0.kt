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

        assertEquals(0, flow.flowElements.size)
    }

    @Test()
    fun onMessageType() {

        val flow = define <PaymentRetrieval> {
            on message RetrievePayment::class
        }

        val node = flow.flowElements[0] as FlowMessageReactionDefinition

        assertEquals(RetrievePayment::class, node.payloadType)

    }

    @Test()
    fun create() {

        val flow = define <PaymentRetrieval> {
            create
        }

        val node = flow.flowElements.get(0)

        assertTrue(node is FlowActionDefinition)

    }

    @Test()
    fun createSuccessWithoutAction() {

        val flow = define <PaymentRetrieval> {
            create success "PaymentRetrieved"
        }

        val node = flow.flowElements.get(0) as FlowActionDefinition

        assertEquals(null, node.function)
    }

    @Test()
    fun createSuccessWithAction() {

        val flow = define <PaymentRetrieval> {
            create success "PaymentRetrieved" by { PaymentRetrieved() }
        }

        val node = flow.flowElements.get(0) as FlowActionDefinition

        assertEquals(FlowActionType.Success, node.flowActionType)
        assertEquals(PaymentRetrieved::class, node.function!!.invoke(PaymentRetrieval(RetrievePayment()))::class)

    }

    @Test()
    fun executeService() {

        val flow = define <PaymentRetrieval> {
            execute service {}
        }

        val node = flow.flowElements.get(0) as FlowDefinition

        assertEquals(FlowExecutionType.service, node.flowExecutionType)

    }

    @Test()
    fun executeServiceCreateIntent() {

        val flow = define <PaymentRetrieval> {
            execute service {
                create intent "ChargeCreditCard" by { ChargeCreditCard() }
            }
        }

        val parentNode = flow.flowElements.get(0) as FlowDefinition
        val node = parentNode.flowElements.get(0)
        val action = node as FlowActionDefinition

        assertEquals(FlowActionType.Intent, action.flowActionType)
        assertEquals(ChargeCreditCard::class, action.function!!.invoke(PaymentRetrieval(RetrievePayment()))::class)
    }

    @Test()
    fun executeServiceOnMessage() {

        val flow = define <PaymentRetrieval> {
            execute service {
                on message CreditCardCharged::class create success("Credit card charged")
            }
        }

        val parentNode = flow.flowElements.get(0) as FlowDefinition
        val node = parentNode.flowElements.get(0) as FlowMessageReactionDefinition

        assertEquals(CreditCardCharged::class, node.payloadType)
        assertEquals(FlowActionType.Success, node.flowReactionAction.flowActionType)
    }


    @Test()
    fun parentsAndElements() {

        val flow = define <PaymentRetrieval> {

            on message (RetrievePayment::class) create acceptance("PaymentRetrievalAccepted") by { PaymentRetrievalAccepted() }

            execute service {

                create intent "ChargeCreditCard" by { ChargeCreditCard() }
                on message (CreditCardCharged::class) having "reference" match { paymentId }

            }

            create success "PaymentRetrieved" by { PaymentRetrieved() }

        }

        assertEquals(3, flow.flowElements.size)
        println(flow.flowElementId)
        flow.flowElements.forEach {
            assertEquals(flow, it.container)
            println(it.flowElementId)
        }

        val service = flow.flowElements[1] as FlowDefinition
        assertEquals(2, service.flowElements.size)
        println(service.flowElementId)
        service.flowElements.forEach {
            assertEquals(service, it.container)
            println(it.flowElementId)
        }

    }

}
