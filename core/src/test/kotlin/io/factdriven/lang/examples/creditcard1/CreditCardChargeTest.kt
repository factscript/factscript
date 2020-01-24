package io.factdriven.lang.examples.creditcard1

import io.factdriven.def.Definition
import io.factdriven.lang.Flow
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest {

    @Test
    fun testDefinition() {

        val definition = Definition.getDefinitionByType(CreditCardCharge::class)
        Assertions.assertEquals(CreditCardCharge::class, definition.entityType)
        Assertions.assertEquals(3, definition.children.size)

        val on = definition.getCatching(ChargeCreditCard::class)
        Assertions.assertEquals(definition.children[0], on)
        Assertions.assertEquals(CreditCardCharge::class, on.entityType)
        Assertions.assertEquals(ChargeCreditCard::class, on.catchingType)
        Assertions.assertEquals(definition, on.parent)

        val notice = definition.getCatching(ConfirmationReceived::class)
        Assertions.assertEquals(definition.children[1], notice)
        Assertions.assertEquals(CreditCardCharge::class, on.entityType)
        Assertions.assertEquals(ChargeCreditCard::class, on.catchingType)
        Assertions.assertEquals(definition, on.parent)

        val emit = definition.getThrowing(CreditCardCharged::class)
        Assertions.assertEquals(definition.children[2], emit)
        Assertions.assertEquals(CreditCardCharge::class, emit.entityType)
        Assertions.assertEquals(CreditCardCharged::class, emit.throwingType)
        Assertions.assertEquals(CreditCardCharged("reference", 3F), emit.constructor.invoke(CreditCardCharge(
            ChargeCreditCard("reference", 3F)
        )))
        Assertions.assertEquals(definition, emit.parent)

    }

}