package io.factdriven.language.definition.await_time

import io.factdriven.language.*
import io.factdriven.language.definition.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest {

    @Test
    fun testDefinition() {

        val definition = Flows.get(CreditCardCharge::class)
        assertEquals(CreditCardCharge::class, definition.entity)
        assertEquals(4, definition.children.size)

        val on = definition.find(nodeOfType = Consuming::class, dealingWith = ChargeCreditCard::class)
        assertEquals(definition.children[0], on)
        assertEquals(CreditCardCharge::class, on?.entity)
        assertEquals(ChargeCreditCard::class, on?.consuming)
        assertEquals(definition, on?.parent)

        /*
            val on = definition.find(nodeOfType = AwaitingTime::class)
            assertEquals(definition.children[0], on)
            assertEquals(CreditCardCharge::class, on?.entity)
            assertEquals(Timer.Cycle, on?.timer)
            assertEquals("P1Y", on?.period?.invoke(CreditCardCharge(ChargeCreditCard("reference", 3F))))
            assertNotNull(on?.from?.invoke(CreditCardCharge(ChargeCreditCard("reference", 3F))))
            assertEquals(3, on?.times?.invoke(CreditCardCharge(ChargeCreditCard("reference", 3F))))
            assertEquals(LocalDate.of(2030, 12, 6).atStartOfDay(), on?.limit?.invoke(CreditCardCharge(ChargeCreditCard("reference", 3F))))
            assertEquals(definition, on?.parent)
        */

        val execute = definition.find(nodeOfType = Executing::class)
        assertEquals(definition.children[1], execute)
        assertEquals(CreditCardCharge::class, execute?.entity)
        assertEquals(ChargeCreditCard::class, execute?.throwing)
        assertEquals(ChargeCreditCard("reference", 3F), execute?.factory?.invoke(CreditCardCharge(ChargeCreditCard("reference", 3F))))
        assertEquals(definition, execute?.parent)

        val but = definition.children[1].children[0]
        assertEquals(definition.children[1], but.parent)
        assertEquals(CreditCardCharge::class, but.entity)

        val duration = but.children[0] as Waiting?
        assertEquals(but, duration?.parent)
        assertEquals(CreditCardCharge::class, duration?.entity)
        assertEquals(Timer.Duration, duration?.timer)
        assertEquals("PT30S", duration?.period?.invoke(CreditCardCharge(ChargeCreditCard("reference", 3F))))
        assertNull(duration?.limit)

        val await = definition.filter(nodesOfType = Waiting::class)[0]
        assertEquals(definition.children[2], await)
        assertEquals(CreditCardCharge::class, await.entity)
        assertEquals(Timer.Limit, await.timer)
        assertEquals(null, await.period)
        assertEquals(LocalDate.of(2030, 12, 6).atStartOfDay(), await.limit?.invoke(CreditCardCharge(ChargeCreditCard("reference", 3F))))
        assertEquals(definition, await.parent)

        val emit = definition.find(nodeOfType = Throwing::class, dealingWith = CreditCardCharged::class)
        assertEquals(definition.children[3], emit)
        assertEquals(CreditCardCharge::class, emit?.entity)
        assertEquals(CreditCardCharged::class, emit?.throwing)
        assertEquals(CreditCardCharged("reference", 3F), emit?.factory?.invoke(CreditCardCharge(
            ChargeCreditCard("reference", 3F)
        )))
        assertEquals(definition, emit?.parent)

    }

}