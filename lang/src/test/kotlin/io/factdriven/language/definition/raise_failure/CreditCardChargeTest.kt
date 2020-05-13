package io.factdriven.language.definition.raise_failure

import io.factdriven.language.*
import io.factdriven.language.definition.*
import io.factdriven.language.impl.utils.asType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest {

    init {
        Flows.activate(CreditCardCharge::class)
    }

    @Test
    fun testDefinition() {

        val definition = Flows.get(CreditCardCharge::class)
        assertEquals(CreditCardCharge::class, definition.entity)
        assertEquals(4, definition.children.size)

        val instance = CreditCardCharge(ChargeCreditCard("1234432112344321", 3F))

        val on = definition.children[0] as Promising
        assertEquals(CreditCardCharge::class, on.entity)
        assertEquals(ChargeCreditCard::class, (on as Consuming).consuming)
        assertEquals(CreditCardCharged::class, on.successType)
        assertEquals(1, on.failureTypes.size)
        assertEquals(CreditCardExpired::class, on.failureTypes.first())
        assertEquals(definition, on.parent)

        val await = definition.children[1] as Correlating
        assertEquals(CreditCardCharge::class, await.entity)
        assertEquals(CreditCardGatewayConfirmationReceived::class, await.consuming)
        assertEquals("1234432112344321", await.correlating.values.iterator().next().invoke(instance))
        assertEquals(1, await.correlating.size)
        assertEquals("reference", await.correlating.keys.iterator().next())
        assertEquals(definition, await.parent)

        val branching = definition.children[2] as Branching
        assertEquals(CreditCardCharge::class, branching.entity)
        assertEquals(Junction.One, branching.fork)
        assertEquals("Credit card expired?", branching.description)

        assertEquals(2, branching.children.size)

        assertEquals(null, branching.children[0].asType<OptionalFlow>()?.condition?.invoke(instance))
        assertEquals(true, branching.children[0].asType<OptionalFlow>()?.isDefault())
        assertEquals(false, branching.children[0].asType<OptionalFlow>()?.isSucceeding())
        assertEquals(false, branching.children[0].asType<OptionalFlow>()?.isFailing())
        assertEquals(true, branching.children[0].asType<OptionalFlow>()?.isContinuing())

        assertEquals(true, branching.children[1].asType<OptionalFlow>()?.condition?.invoke(instance))
        assertEquals(false, branching.children[1].asType<OptionalFlow>()?.isDefault())
        assertEquals(false, branching.children[1].asType<OptionalFlow>()?.isSucceeding())
        assertEquals(true, branching.children[1].asType<OptionalFlow>()?.isFailing())
        assertEquals(false, branching.children[1].asType<OptionalFlow>()?.isContinuing())

        val emit = definition.children[3] as Throwing
        assertEquals(CreditCardCharge::class, emit.entity)
        assertEquals(CreditCardCharged::class, emit.throwing)
        assertEquals(false, emit.isContinuing())
        assertEquals(false, emit.isFailing())
        assertEquals(true, emit.isSucceeding())
        assertEquals(CreditCardCharged("1234432112344321", 3F), emit.factory.invoke(instance))
        assertEquals(definition, emit.parent)

    }

}