package io.factdriven.definition

import io.factdriven.Flows
import io.factdriven.flow
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class NodeTest {

    init {
        Flows.initialize(NodeTestFlow::class)
    }

    @Test
    fun testLevel1Forward() {

        var node = Flows.get(NodeTestFlow::class).start as Node?
        assertTrue(node is Promising)
        assertTrue(node!!.isFirstSibling())
        assertTrue(node.isStart())

        node = node.forward
        assertTrue(node is Branching)

        node = node?.forward
        assertTrue(node is Throwing)
        assertTrue(node!!.isLastSibling())
        assertTrue(node.isFinish())

        node = node.forward
        assertTrue(node == null)

    }

    @Test
    fun testLevel1Backward() {

        var node = Flows.get(NodeTestFlow::class).finish as Node?
        assertTrue(node is Throwing)

        node = node?.backward
        assertTrue(node is Branching)
        assertTrue(!node!!.isFirstSibling())
        assertTrue(!node.isStart())

        node = node.backward
        assertTrue(node is Promising)
        assertTrue(node!!.isFirstSibling())
        assertTrue(node.isStart())

        node = node.backward
        assertTrue(node == null)

    }

    @Test
    fun testLevel2Forward() {

        var node = Flows.get(NodeTestFlow::class).start.forward!!.children.first().children.first() as Node?
        assertTrue(node is Executing)
        assertTrue(!node!!.isLastSibling())
        assertTrue(!node.isFinish())

        node = node.forward
        assertTrue(node is Executing)
        assertTrue(node!!.isLastSibling())
        assertTrue(!node.isFinish())

        node = node.forward
        assertTrue(node is Throwing)
        assertTrue(node!!.isLastSibling())
        assertTrue(node.isFinish())

        node = node.forward
        assertTrue(node == null)

    }

    @Test
    fun testLevel2Backward() {

        var node = Flows.get(NodeTestFlow::class).start.forward!!.children.first().children.first().forward as Node?
        assertTrue(node is Executing)
        assertTrue(!node!!.isFirstSibling())
        assertTrue(!node.isStart())

        node = node.backward
        assertTrue(node is Executing)
        assertTrue(node!!.isFirstSibling())
        assertTrue(!node.isStart())

        node = node.backward
        assertTrue(node is Promising)
        assertTrue(node!!.isFirstSibling())
        assertTrue(node.isStart())

        node = node.backward
        assertTrue(node == null)

    }

}

class NodeTestFlow(fact: RetrievePayment) {

    var id = UUID.randomUUID().toString()
    var total = fact.amount
    var covered = 0F

    companion object {

        init {

            flow<NodeTestFlow> {

                on command RetrievePayment::class

                execute all {
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(id, total - covered)
                    }
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(id, total - covered)
                    }
                } and {
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(id, total - covered)
                    }
                }

                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(total)
                }

            }

        }

    }

}

data class CreditCardCharge(val fact: ChargeCreditCard) {

    private val reference: String = fact.reference
    private val amount: Float = fact.amount
    private var successful: Boolean = false

    fun apply(fact: CreditCardCharged) {
        successful = true
    }

    companion object {

        init {

            flow<CreditCardCharge> {

                on command ChargeCreditCard::class promise {
                    report success CreditCardCharged::class
                }

                consume event (CreditCardGatewayConfirmationReceived::class) having "reference" match { reference }

                emit event CreditCardCharged::class by {
                    CreditCardCharged(reference, amount)
                }

            }

        }

    }

}

data class ChargeCreditCard(val reference: String, val amount: Float)
data class CreditCardCharged(val reference: String, val amount: Float)
data class CreditCardGatewayConfirmationReceived(val reference: String, val amount: Float)