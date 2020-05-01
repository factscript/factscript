package io.factdriven.language.definition.await_time

import io.factdriven.language.Flows
import io.factdriven.language.definition.Catching
import io.factdriven.language.definition.Throwing
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest {

    @Test @Disabled
    fun testDefinition() {

        val definition = Flows.get(CreditCardCharge::class)
        assertEquals(CreditCardCharge::class, definition.entity)
        assertEquals(3, definition.children.size)

        val on = definition.find(nodeOfType = Catching::class, dealingWith = ChargeCreditCard::class)
        assertEquals(definition.children[0], on)
        assertEquals(CreditCardCharge::class, on?.entity)
        assertEquals(ChargeCreditCard::class, on?.catching)
        assertEquals(definition, on?.parent)

        val notice = definition.find(nodeOfType = Catching::class, dealingWith = ConfirmationReceived::class)
        assertEquals(definition.children[1], notice)
        assertEquals(CreditCardCharge::class, on?.entity)
        assertEquals(ChargeCreditCard::class, on?.catching)
        assertEquals(definition, on?.parent)

        val emit = definition.find(nodeOfType = Throwing::class, dealingWith = CreditCardCharged::class)
        assertEquals(definition.children[2], emit)
        assertEquals(CreditCardCharge::class, emit?.entity)
        assertEquals(CreditCardCharged::class, emit?.throwing)
        assertEquals(CreditCardCharged("reference", 3F), emit?.instance?.invoke(CreditCardCharge(
            ChargeCreditCard("reference", 3F)
        )))
        assertEquals(definition, emit?.parent)

    }

}