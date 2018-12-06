package io.factdriven.flow.lang.examples

import io.factdriven.flow.lang.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalStep0Test {

    @Test()
    fun execute() {
        val flow = execute<PaymentRetrieval> {
        }
        assertNotNull(flow)
    }

    @Test()
    fun on() {
        val flow = execute<PaymentRetrieval> {
            on
        } as FlowDefinition<PaymentRetrieval>
        assertEquals(0, flow.nodes.size)
    }

    @Test()
    fun onMessageType() {
        val flow = execute<PaymentRetrieval> {
            on message type(RetrievePayment::class)
        } as FlowDefinition<PaymentRetrieval>
        val node = flow.nodes.get(0) as FlowReactionImpl<*, *>
        assertEquals(FlowReactionImpl::class, node::class)
        val listener = (node as FlowReactionImpl).listener
        assertEquals(RetrievePayment::class, listener.type)
    }

    @Test()
    fun create() {
        val flow = execute<PaymentRetrieval> {
            create
        } as FlowDefinition<PaymentRetrieval>
        val node = flow.nodes.get(0)
        assertEquals(FlowActionImpl::class, node::class)
    }

    @Test()
    fun createSuccessWithoutAction() {
        val flow = execute<PaymentRetrieval> {
            create success("PaymentRetrieved")
        } as FlowDefinition<PaymentRetrieval>
        val node = flow.nodes.get(0) as FlowActionImpl<*, *>
        assertEquals(null, node.action)
    }

    @Test()
    fun createSuccessWithAction() {
        val flow = execute<PaymentRetrieval> {
            create success("PaymentRetrieved") by { PaymentRetrieved() }
        } as FlowDefinition<PaymentRetrieval>
        val node = flow.nodes.get(0) as FlowActionImpl<*, *>
        assertEquals(FlowActionType.success, node.actionType)
        assertEquals(PaymentRetrieved::class, node.action!!.invoke()::class)
    }

    @Test()
    fun executeService() {
        val flow = execute<PaymentRetrieval> {
            execute service {}
        } as FlowDefinition<PaymentRetrieval>
        val node = flow.nodes.get(0) as FlowDefinition<*>
        assertEquals(FlowDefinitionType.service, node.type)
    }

    @Test()
    fun executeServiceCreateIntent() {
        val flow = execute<PaymentRetrieval> {
            execute service {
                create intent("ChargeCreditCard") by { ChargeCreditCard() }
            }
        } as FlowDefinition<PaymentRetrieval>
        val parentNode = flow.nodes.get(0) as FlowDefinition<*>
        val node = parentNode.nodes.get(0)
        assertEquals(FlowActionImpl::class, node::class)
        val action = node as FlowActionImpl<*, *>
        assertEquals(FlowActionType.intent, action.actionType)
        assertEquals(ChargeCreditCard::class, action.action!!.invoke()::class)
    }

    @Test()
    fun executeServiceOnMessage() {
        val flow = execute<PaymentRetrieval> {
            execute service {
                on message type(CreditCardCharged::class) create success()
            }
        } as FlowDefinition<PaymentRetrieval>
        val parentNode = flow.nodes.get(0) as FlowDefinition<*>
        val node = parentNode.nodes.get(0) as FlowReactionImpl<*, *>
        val reaction = node as FlowReactionImpl
        assertEquals(CreditCardCharged::class, reaction.listener.type)
        assertEquals(FlowActionType.success, reaction.actionType)
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
        } as FlowDefinition<PaymentRetrieval>
        assertEquals(3, flow.nodes.size)
        val service = flow.nodes[1] as FlowDefinition<*>
        assertEquals(2, service.nodes.size)
    }

}
