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

        val node = flow.children[0] as DefinedMessageReaction

        assertEquals(RetrievePayment::class, node.factType)

    }

    @Test()
    fun create() {

        val flow = define <PaymentRetrieval> {
            create
        }

        val node = flow.children.get(0)

        assertTrue(node is DefinedAction)

    }

    @Test()
    fun createSuccessWithoutAction() {

        val flow = define <PaymentRetrieval> {
            create success "PaymentRetrieved"
        }

        val node = flow.children.get(0) as DefinedAction

        assertEquals(null, node.function)
    }

    @Test()
    fun createSuccessWithAction() {

        val flow = define <PaymentRetrieval> {
            create success (PaymentRetrieved::class) by { PaymentRetrieved() }
        }

        val node = flow.children.get(0) as DefinedAction

        assertEquals(ActionClassifier.Success, node.classifier)
        assertEquals(PaymentRetrieved::class, node.function!!.invoke(PaymentRetrieval(RetrievePayment(payment = 2F)))::class)

    }

    @Test()
    fun executeService() {

        val flow = define <PaymentRetrieval> {
            execute service {}
        }

        val node = flow.children.get(0) as DefinedFlow<*>

        assertEquals(FlowClassifier.Service, node.classifier)

    }

    @Test()
    fun executeServiceCreateIntent() {

        val flow = define <PaymentRetrieval> {
            execute service {
                create intention ChargeCreditCard::class by { ChargeCreditCard() }
            }
        }

        val parentNode = flow.children.get(0) as DefinedFlow<*>
        val node = parentNode.children.get(0)
        val action = node as DefinedAction

        assertEquals(ActionClassifier.Intention, action.classifier)
        assertEquals(ChargeCreditCard::class, action.function!!.invoke(PaymentRetrieval(RetrievePayment(payment = 2F)))::class)
    }

    @Test()
    fun executeServiceOnMessage() {

        val flow = define <PaymentRetrieval> {
            execute service {
                on message CreditCardCharged::class create success("Credit card charged")
            }
        }

        val parentNode = flow.children.get(0) as DefinedFlow<*>
        val node = parentNode.children.get(0) as DefinedMessageReaction

        assertEquals(CreditCardCharged::class, node.factType)
        assertEquals(ActionClassifier.Success, node.action!!.classifier)
    }


    @Test()
    fun parentsAndElements() {

        val flow = define <PaymentRetrieval> {

            on message (RetrievePayment::class) create this.progress(PaymentRetrievalAccepted::class) by { PaymentRetrievalAccepted() }

            execute service {

                create intention ChargeCreditCard::class by { ChargeCreditCard() }
                on message (CreditCardCharged::class) having "reference" match { paymentId }

            }

            create success PaymentRetrieved::class by { PaymentRetrieved() }

        }

        assertEquals(3, flow.children.size)
        flow.children.forEach {
            assertEquals(flow, it.parent)
        }

        val service = flow.children[1] as DefinedFlow<*>
        assertEquals(2, service.children.size)
        service.children.forEach {
            assertEquals(service, it.parent)
        }

    }

}
