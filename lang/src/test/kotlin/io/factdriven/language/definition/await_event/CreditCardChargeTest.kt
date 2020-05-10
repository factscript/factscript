package io.factdriven.language.definition.await_event

import io.factdriven.language.Flows
import io.factdriven.language.definition.Consuming
import io.factdriven.language.definition.Throwing
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest {

    @Test
    fun testDefinition() {

        val definition = Flows.get(CreditCardCharge::class)
        Assertions.assertEquals(CreditCardCharge::class, definition.entity)
        Assertions.assertEquals(3, definition.children.size)

        val on = definition.find(nodeOfType = Consuming::class, dealingWith = ChargeCreditCard::class)
        Assertions.assertEquals(definition.children[0], on)
        Assertions.assertEquals(CreditCardCharge::class, on?.entity)
        Assertions.assertEquals(ChargeCreditCard::class, on?.consuming)
        Assertions.assertEquals(definition, on?.parent)

        val notice = definition.find(nodeOfType = Consuming::class, dealingWith = ConfirmationReceived::class)
        Assertions.assertEquals(definition.children[1], notice)
        Assertions.assertEquals(CreditCardCharge::class, on?.entity)
        Assertions.assertEquals(ChargeCreditCard::class, on?.consuming)
        Assertions.assertEquals(definition, on?.parent)

        val emit = definition.find(nodeOfType = Throwing::class, dealingWith = CreditCardCharged::class)
        Assertions.assertEquals(definition.children[2], emit)
        Assertions.assertEquals(CreditCardCharge::class, emit?.entity)
        Assertions.assertEquals(CreditCardCharged::class, emit?.throwing)
        Assertions.assertEquals(CreditCardCharged("reference", 3F), emit?.factory?.invoke(CreditCardCharge(
            ChargeCreditCard("reference", 3F)
        )))
        Assertions.assertEquals(definition, emit?.parent)

    }

}