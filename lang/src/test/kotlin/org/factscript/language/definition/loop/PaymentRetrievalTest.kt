package org.factscript.language.definition.loop

import org.factscript.language.*
import org.factscript.language.Flows.activate
import org.factscript.language.definition.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    init { activate(PaymentRetrieval::class, CustomerAccount::class, CreditCardCharge::class) }

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

        val loop = definition.children[1] as RepeatingFlow
        assertEquals(PaymentRetrieval::class, loop.entity)
        assertEquals("Payment covered?", loop.description)
        assertEquals(2, loop.children.size)
        assertTrue(Executing::class.isInstance(loop.children[0]))
        assertTrue(Conditional::class.isInstance(loop.children[1]))

        val emit = definition.children[2] as Throwing
        assertEquals(PaymentRetrieval::class, emit.entity)
        assertEquals(PaymentRetrieved::class, emit.throwing)
        assertEquals(PaymentRetrieved(3F), emit.factory.invoke(instance))
        assertEquals(definition, emit.parent)


    }

}