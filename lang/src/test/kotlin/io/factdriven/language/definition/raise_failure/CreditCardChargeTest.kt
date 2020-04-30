package io.factdriven.language.definition.raise_failure

import io.factdriven.language.Flows
import io.factdriven.language.definition.*
import io.factdriven.language.impl.utils.asType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest {

    init {
        Flows.initialize(CreditCardCharge::class)
    }

    @Test
    fun testDefinition() {

        val definition = Flows.get(CreditCardCharge::class)
        assertEquals(CreditCardCharge::class, definition.entity)
        assertEquals(4, definition.children.size)

        val instance = CreditCardCharge(ChargeCreditCard("1234432112344321", 3F))

        val on = definition.children[0] as Promising
        assertEquals(CreditCardCharge::class, on.entity)
        assertEquals(ChargeCreditCard::class, on.catching)
        assertEquals(CreditCardCharged::class, on.succeeding)
        assertEquals(1, on.failing.size)
        assertEquals(CreditCardExpired::class, on.failing.first())
        assertEquals(definition, on.parent)

        val await = definition.children[1] as Awaiting
        assertEquals(CreditCardCharge::class, await.entity)
        assertEquals(CreditCardGatewayConfirmationReceived::class, await.catching)
        assertEquals(1, await.matching.size)
        assertEquals("1234432112344321", await.matching.first().invoke(instance))
        assertEquals(1, await.properties.size)
        assertEquals("reference", await.properties.first())
        assertEquals(definition, await.parent)

        val branching = definition.children[2] as Branching
        assertEquals(CreditCardCharge::class, branching.entity)
        assertEquals(Gateway.Exclusive, branching.gateway)
        assertEquals("Credit card expired?", branching.label)

        assertEquals(2, branching.children.size)

        assertEquals(null, branching.children[0].asType<ConditionalFlow>()?.condition?.invoke(instance))
        assertEquals(true, branching.children[0].asType<ConditionalFlow>()?.isDefault())
        assertEquals(false, branching.children[0].asType<ConditionalFlow>()?.isSucceeding())
        assertEquals(false, branching.children[0].asType<ConditionalFlow>()?.isFailing())
        assertEquals(true, branching.children[0].asType<ConditionalFlow>()?.isContinuing())

        assertEquals(true, branching.children[1].asType<ConditionalFlow>()?.condition?.invoke(instance))
        assertEquals(false, branching.children[1].asType<ConditionalFlow>()?.isDefault())
        assertEquals(false, branching.children[1].asType<ConditionalFlow>()?.isSucceeding())
        assertEquals(true, branching.children[1].asType<ConditionalFlow>()?.isFailing())
        assertEquals(false, branching.children[1].asType<ConditionalFlow>()?.isContinuing())

        val emit = definition.children[3] as Throwing
        assertEquals(CreditCardCharge::class, emit.entity)
        assertEquals(CreditCardCharged::class, emit.throwing)
        assertEquals(false, emit.isContinuing())
        assertEquals(false, emit.isFailing())
        assertEquals(true, emit.isSucceeding())
        assertEquals(CreditCardCharged("1234432112344321", 3F), emit.instance.invoke(instance))
        assertEquals(definition, emit.parent)

    }

}