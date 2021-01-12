package org.factscript.language.definition.execute_all

import org.factscript.language.*
import org.factscript.language.definition.Consuming
import org.factscript.language.definition.Branching
import org.factscript.language.definition.Junction
import org.factscript.language.definition.Throwing
import org.factscript.language.Execution
import org.factscript.language.Flows.activate
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

        val branching = definition.children[1] as Branching
        assertEquals(PaymentRetrieval::class, branching.entity)
        assertEquals(Junction.All, branching.fork)
        assertEquals("", branching.description)
        assertEquals(2, branching.children.size)
        assertTrue(Execution::class.isInstance(branching.children[0]))
        assertTrue(Execution::class.isInstance(branching.children[1]))

        val emit = definition.children[2] as Throwing
        assertEquals(PaymentRetrieval::class, emit.entity)
        assertEquals(PaymentRetrieved::class, emit.throwing)
        assertEquals(PaymentRetrieved(3F), emit.factory.invoke(instance))
        assertEquals(definition, emit.parent)


    }

}