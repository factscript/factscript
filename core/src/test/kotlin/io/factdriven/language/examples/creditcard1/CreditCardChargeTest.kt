package io.factdriven.language.examples.creditcard1

import io.factdriven.definition.Flows
import io.factdriven.definition.getCatching
import io.factdriven.definition.getThrowing
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest {

    @Test
    fun testDefinition() {

        val definition = Flows.getDefinitionByType(CreditCardCharge::class)
        Assertions.assertEquals(CreditCardCharge::class, definition.entity)
        Assertions.assertEquals(3, definition.children.size)

        val on = definition.getCatching(ChargeCreditCard::class)
        Assertions.assertEquals(definition.children[0], on)
        Assertions.assertEquals(CreditCardCharge::class, on.entity)
        Assertions.assertEquals(ChargeCreditCard::class, on.catching)
        Assertions.assertEquals(definition, on.parent)

        val notice = definition.getCatching(ConfirmationReceived::class)
        Assertions.assertEquals(definition.children[1], notice)
        Assertions.assertEquals(CreditCardCharge::class, on.entity)
        Assertions.assertEquals(ChargeCreditCard::class, on.catching)
        Assertions.assertEquals(definition, on.parent)

        val emit = definition.getThrowing(CreditCardCharged::class)
        Assertions.assertEquals(definition.children[2], emit)
        Assertions.assertEquals(CreditCardCharge::class, emit.entity)
        Assertions.assertEquals(CreditCardCharged::class, emit.throwing)
        Assertions.assertEquals(CreditCardCharged("reference", 3F), emit.instance.invoke(CreditCardCharge(
            ChargeCreditCard("reference", 3F)
        )))
        Assertions.assertEquals(definition, emit.parent)

    }

}