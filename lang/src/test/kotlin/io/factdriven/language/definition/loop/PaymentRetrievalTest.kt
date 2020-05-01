package io.factdriven.language.definition.loop

import io.factdriven.language.Flows
import io.factdriven.language.definition.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    init {
        Flows.initialize(PaymentRetrieval::class)
        Flows.initialize(CustomerAccount::class)
        Flows.initialize(CreditCardCharge::class)
    }

    @Test
    fun testDefinition() {

        val definition = Flows.get(PaymentRetrieval::class)
        assertEquals(PaymentRetrieval::class, definition.entity)
        assertEquals(3, definition.children.size)

        val instance = PaymentRetrieval(RetrievePayment(3F))

        val on = definition.children[0] as Consuming
        assertEquals(PaymentRetrieval::class, on.entity)
        assertEquals(RetrievePayment::class, on.consuming)
        assertEquals(definition, on.parent)

        val loop = definition.children[1] as Looping
        assertEquals(PaymentRetrieval::class, loop.entity)
        assertEquals("Payment retrieval", loop.description)
        assertEquals(2, loop.children.size)
        assertTrue(Calling::class.isInstance(loop.children[0]))
        assertTrue(Conditional::class.isInstance(loop.children[1]))

        val emit = definition.children[2] as Throwing
        assertEquals(PaymentRetrieval::class, emit.entity)
        assertEquals(PaymentRetrieved::class, emit.throwing)
        assertEquals(PaymentRetrieved(3F), emit.instance.invoke(instance))
        assertEquals(definition, emit.parent)


    }

}