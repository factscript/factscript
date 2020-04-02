package io.factdriven.language.examples.payment4

import io.factdriven.definition.*
import io.factdriven.definition.api.Catching
import io.factdriven.definition.api.Branching
import io.factdriven.definition.api.Gateway
import io.factdriven.definition.api.Throwing
import io.factdriven.language.ConditionalExecution
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    init {
        Definitions.init(PaymentRetrieval::class)
        Definitions.init(CustomerAccount::class)
        Definitions.init(CreditCardCharge::class)
    }

    @Test
    fun testDefinition() {

        val all = Definitions.all

        val definition = Definitions.getDefinitionByType(PaymentRetrieval::class)
        assertEquals(PaymentRetrieval::class, definition.entityType)
        assertEquals(3, definition.children.size)

        val instance = PaymentRetrieval(RetrievePayment(3F))

        val on = definition.children[0] as Catching
        assertEquals(PaymentRetrieval::class, on.entityType)
        assertEquals(RetrievePayment::class, on.catching)
        assertEquals(definition, on.parent)

        val branching = definition.children[1] as Branching
        assertEquals(PaymentRetrieval::class, branching.entityType)
        assertEquals(Gateway.Exclusive, branching.gateway)
        assertEquals("Payment (partly) uncovered?", branching.label)
        assertEquals(2, branching.children.size)
        Assertions.assertTrue(ConditionalExecution::class.isInstance(branching.children[0]))
        Assertions.assertTrue(ConditionalExecution::class.isInstance(branching.children[1]))

        val emit = definition.children[2] as Throwing
        assertEquals(PaymentRetrieval::class, emit.entityType)
        assertEquals(PaymentRetrieved::class, emit.throwing)
        assertEquals(PaymentRetrieved(3F), emit.instance.invoke(instance))
        assertEquals(definition, emit.parent)


    }

}