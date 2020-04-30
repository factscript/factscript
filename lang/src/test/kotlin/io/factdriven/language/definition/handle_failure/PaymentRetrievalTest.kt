package io.factdriven.language.definition.handle_failure

import io.factdriven.language.Flows
import io.factdriven.language.definition.Calling
import io.factdriven.language.definition.Promising
import io.factdriven.language.definition.Throwing
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    init {
        Flows.initialize(PaymentRetrieval::class)
        Flows.initialize(CreditCardCharge::class)
    }

    @Test
    fun testDefinition() {

        val definition = Flows.get(PaymentRetrieval::class)
        assertEquals(PaymentRetrieval::class, definition.entity)
        assertEquals(3, definition.children.size)

        val instance = PaymentRetrieval(RetrievePayment(3F))

        assertEquals(true, definition.isSucceeding())
        assertEquals(false, definition.isContinuing())

        val on = definition.children[0] as Promising
        assertEquals(PaymentRetrieval::class, on.entity)
        assertEquals(RetrievePayment::class, on.catching)
        assertEquals(PaymentRetrieved::class, on.succeeding)
        assertEquals(1, on.failing.size)
        assertEquals(PaymentFailed::class, on.failing.first())
        assertEquals(definition, on.parent)

        val execute = definition.find(nodeOfType = Calling::class, dealingWith = ChargeCreditCard::class)
        assertEquals(PaymentRetrieval::class, execute?.entity)
        assertEquals(ChargeCreditCard::class, execute?.throwing)
        assertEquals(CreditCardCharged::class, execute?.catching)
        assertEquals(CreditCardCharged::class, execute?.succeeding)
        assertEquals(1, execute?.failing?.size)
        assertEquals(CreditCardExpired::class, execute?.failing?.first())
        assertEquals(ChargeCreditCard(instance.id, instance.total), execute?.instance?.invoke(instance))
        assertEquals(definition, execute?.parent)

        val emit = definition.find(nodeOfType = Throwing::class, dealingWith = PaymentRetrieved::class)
        assertEquals(PaymentRetrieval::class, emit?.entity)
        assertEquals(PaymentRetrieved::class, emit?.throwing)
        assertEquals(PaymentRetrieved(3F), emit?.instance?.invoke(instance))
        assertEquals(definition, emit?.parent)

    }

}