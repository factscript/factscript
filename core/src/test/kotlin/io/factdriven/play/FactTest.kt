package io.factdriven.play

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class FactTest {

    data class SomeFact(val property: String)

    @Test
    fun testFact() {

        val instance = SomeFact("value")
        val fact = Fact(instance)
        assertNotNull(fact.id)
        assertEquals("SomeFact", fact.name)
        assertEquals(SomeFact::class, fact.type)
        assertEquals(instance, fact.details)

    }

    @Test
    fun testJson() {

        val fact = Fact(SomeFact("value"))
        assertEquals(fact, Fact.fromJson(fact.toJson()))

    }

}
