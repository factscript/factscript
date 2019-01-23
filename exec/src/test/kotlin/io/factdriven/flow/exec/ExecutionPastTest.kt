package io.factdriven.flow.exec

import io.factdriven.flow.execute
import io.factdriven.flow.lang.Message
import io.factdriven.flow.past
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class ExecutionPastTest {

    private val flow = execute <PaymentRetrieval> {}

    @Test
    fun testPastWithEmptyMessages() {

        val messages = emptyList<Message>()
        val aggregate = past(messages, flow)

        assertNull(aggregate)

    }

    @Test
    fun testPastWithOneMessage() {

        val messages = listOf(
            RetrievePayment(id = "anId", accountId = "anAccountId", payment = 3F)
        )
        val aggregate = past(messages, flow)

        assertEquals("anId", aggregate!!.paymentId)
        assertEquals("anAccountId", aggregate.accountId)
        assertEquals(3F, aggregate.uncovered)
        assertEquals(0F, aggregate.covered)

    }

    @Test
    fun testPastWithTwoMessages() {

        val messages = listOf(
            RetrievePayment(id = "anId", accountId = "anAccountId", payment = 3F),
            PaymentRetrieved()
        )
        val aggregate = past(messages, flow)

        assertEquals("anId", aggregate!!.paymentId)
        assertEquals("anAccountId", aggregate.accountId)
        assertEquals(0F, aggregate.uncovered)
        assertEquals(3F, aggregate.covered)

    }

    @Test
    fun testPastWithThreeMessages() {

        val messages = listOf(
            RetrievePayment(id = "anId", accountId = "anAccountId", payment = 3F),
            PaymentRetrieved(payment = 1F),
            PaymentRetrieved(payment = 1F)
        )
        val aggregate = past(messages, flow)

        assertEquals("anId", aggregate!!.paymentId)
        assertEquals("anAccountId", aggregate.accountId)
        assertEquals(1F, aggregate.uncovered)
        assertEquals(2F, aggregate.covered)

    }

}