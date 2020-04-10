package io.factdriven.impl.execution

import io.factdriven.impl.utils.json
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
        assertEquals(Type.from(SomeFact::class), fact.type)
        assertEquals(SomeFact::class, fact.type.kClass)
        assertEquals(instance, fact.details)

    }

    @Test
    fun testJson() {

        val fact = Fact(SomeFact("value"))
        assertEquals(SomeFact("value"), fact.details)
        assertEquals(fact, Fact.fromJson(fact.json))

    }

}

class ApplyFactsToClassTest {

    data class SomeFact(val property: String)
    data class SomeOtherFact(val property: String)

    data class SomeClass(val someFact: SomeFact) {

        val someProperty = someFact.property
        var someOtherProperty: String? = null

        fun apply(someOtherFact: SomeOtherFact) {
            someOtherProperty = someOtherFact.property
        }

    }

    @Test
    fun testApplyTo() {

        val facts = listOf(Fact(SomeFact("value")), Fact(SomeOtherFact("otherValue")))
        val instance = facts.newInstance<SomeClass>()

        assertEquals("value", instance.someProperty)
        assertEquals("otherValue", instance.someOtherProperty)

    }

}