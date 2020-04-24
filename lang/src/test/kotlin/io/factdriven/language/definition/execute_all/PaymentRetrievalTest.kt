package io.factdriven.language.definition.execute_all

import io.factdriven.language.Flows
import io.factdriven.language.definition.Catching
import io.factdriven.language.definition.Branching
import io.factdriven.language.definition.Gateway
import io.factdriven.language.definition.Throwing
import io.factdriven.language.Execution
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

        val on = definition.children[0] as Catching
        assertEquals(PaymentRetrieval::class, on.entity)
        assertEquals(RetrievePayment::class, on.catching)
        assertEquals(definition, on.parent)

        val branching = definition.children[1] as Branching
        assertEquals(PaymentRetrieval::class, branching.entity)
        assertEquals(Gateway.Parallel, branching.gateway)
        assertEquals("", branching.label)
        assertEquals(2, branching.children.size)
        assertTrue(Execution::class.isInstance(branching.children[0]))
        assertTrue(Execution::class.isInstance(branching.children[1]))

        val emit = definition.children[2] as Throwing
        assertEquals(PaymentRetrieval::class, emit.entity)
        assertEquals(PaymentRetrieved::class, emit.throwing)
        assertEquals(PaymentRetrieved(3F), emit.instance.invoke(instance))
        assertEquals(definition, emit.parent)


    }

}