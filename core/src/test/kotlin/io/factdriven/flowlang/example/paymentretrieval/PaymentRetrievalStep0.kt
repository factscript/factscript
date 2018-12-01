package io.factdriven.flowlang.example.paymentretrieval

import io.factdriven.flowlang.*
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
        val node = flow.nodes.get(0)
        assertEquals(FlowTrigger::class, node::class)
    }

    @Test()
    fun onMessageType() {
        val flow = execute <PaymentRetrieval> {
            on message type(RetrievePayment::class)
        }
        val node = flow.nodes.get(0) as FlowTrigger<*>
        assertEquals(FlowReactionToMessage::class, node.reaction::class)
        val listener = (node.reaction as FlowReactionToMessage).listener
        assertEquals(RetrievePayment::class, listener.type)
    }

    @Test()
    fun create() {
        val flow = execute <PaymentRetrieval> {
            create
        }
        val node = flow.nodes.get(0)
        assertEquals(FlowAction::class, node::class)
    }

    @Test()
    fun createSuccessWithoutAction() {
        val flow = execute <PaymentRetrieval> {
            create success {}
        }
        val node = flow.nodes.get(0) as FlowAction<*, *>
        assertEquals(Unit::class, node.action.invoke()::class)
    }

    @Test()
    fun createSuccessWithAction() {
        val flow = execute <PaymentRetrieval> {
            create success { PaymentRetrieved() }
        }
        val node = flow.nodes.get(0) as FlowAction<*, *>
        assertEquals(FlowActionType.success, node.actionType)
        assertEquals(PaymentRetrieved::class, node.action.invoke()::class)
    }

    @Test()
    fun executeService() {
        val flow = execute <PaymentRetrieval> {
            execute service {}
        }
        val node = flow.nodes.get(0)
        assertEquals(FlowExecution::class, node::class)
    }

    @Test()
    fun executeServiceCreateIntent() {
        val flow = execute <PaymentRetrieval> {
            execute service {
                create intent { ChargeCreditCard() }
            }
        }
        val parentNode = flow.nodes.get(0) as FlowExecution<*>
        val node = parentNode.definition.nodes.get(0)
        assertEquals(FlowAction::class, node::class)
        val action = node as FlowAction<*, *>
        assertEquals(FlowActionType.intent, action.actionType)
        assertEquals(ChargeCreditCard::class, action.action.invoke()::class)
    }

    @Test()
    fun executeServiceOnMessage() {
        val flow = execute <PaymentRetrieval> {
            execute service {
                on message type(CreditCardCharged::class) success {}
            }
        }
        val parentNode = flow.nodes.get(0) as FlowExecution<*>
        val node = parentNode.definition.nodes.get(0) as FlowTrigger<*>
        val reaction = node.reaction as FlowReactionToMessage
        assertEquals(CreditCardCharged::class, reaction.listener.type)
        assertEquals(FlowActionType.success, reaction.actionType)
    }


    @Test()
    fun flow() {
        val flow = execute <PaymentRetrieval> {
            on message type(RetrievePayment::class)
            execute service {
                create intent { ChargeCreditCard() }
                on message type(CreditCardCharged::class)
            }
            create success { PaymentRetrieved() }
        }
        assertEquals(3, flow.nodes.size)
        val service = flow.nodes[1] as FlowExecution<*>
        assertEquals(2, service.definition.nodes.size)
    }

}
