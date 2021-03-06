package org.factscript.execution

import org.factscript.language.impl.utils.prettyJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class MessageTest {

    data class SomeFact(val property: String)

    @Test
    fun testMessage() {

        val fact = Fact(SomeFact("value"))
        val message = Message(Any::class, fact)
        assertNotNull(message.id)
        assertEquals(fact, message.fact)

    }

    @Test
    fun testJson() {

        val message = Message(Any::class, Fact(SomeFact("value")))
        assertNotNull(message.prettyJson)
        assertEquals(message, Message.fromJson(message.prettyJson))

    }

    @Test
    fun testJsonList() {

        val messages = listOf(
            Message(Any::class, Fact(SomeFact("value1"))),
            Message(
                Any::class,
                Fact(SomeFact("value2"))
            )
        )
        assertEquals(messages, Messages.fromJson(messages.prettyJson))

    }

}

class ApplyMessagesToClassTest {

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

        val messages = listOf(
            Message(Any::class, Fact(SomeFact("value"))),
            Message(
                Any::class,
                Fact(SomeOtherFact("otherValue"))
            )
        )
        val instance = messages.newInstance<SomeClass>()

        assertEquals("value", instance.someProperty)
        assertEquals("otherValue", instance.someOtherProperty)

    }

}