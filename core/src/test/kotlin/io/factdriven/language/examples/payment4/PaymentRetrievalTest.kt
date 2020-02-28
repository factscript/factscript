package io.factdriven.language.examples.payment4

import io.factdriven.definition.*
import io.factdriven.language.ConditionalExecution
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    init {
        Definition.init(PaymentRetrieval::class)
        Definition.init(CustomerAccount::class)
        Definition.init(CreditCardCharge::class)
    }

    @Test
    fun testDefinition() {

        val all = Definition.all

        val definition = Definition.getDefinitionByType(PaymentRetrieval::class)
        assertEquals(PaymentRetrieval::class, definition.entityType)
        assertEquals(3, definition.children.size)

        val instance = PaymentRetrieval(RetrievePayment(3F))

        val on = definition.children[0] as Catching
        assertEquals(PaymentRetrieval::class, on.entityType)
        assertEquals(RetrievePayment::class, on.catchingType)
        assertEquals(definition, on.parent)

        val gateway = definition.children[1] as Gateway
        assertEquals(PaymentRetrieval::class, gateway.entityType)
        assertEquals(GatewayType.Exclusive, gateway.gatewayType)
        assertEquals("Payment (partly) uncovered?", gateway.label)
        assertEquals(2, gateway.children.size)
        Assertions.assertTrue(ConditionalExecution::class.isInstance(gateway.children[0]))
        Assertions.assertTrue(ConditionalExecution::class.isInstance(gateway.children[1]))

        val emit = definition.children[2] as Throwing
        assertEquals(PaymentRetrieval::class, emit.entityType)
        assertEquals(PaymentRetrieved::class, emit.throwingType)
        assertEquals(PaymentRetrieved(3F), emit.constructor.invoke(instance))
        assertEquals(definition, emit.parent)


    }

}