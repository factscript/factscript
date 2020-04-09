package io.factdriven.execution

import io.factdriven.implementation.utils.json
import org.junit.jupiter.api.Assertions.*
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
        assertNotNull(message.json)
        assertEquals(message, Message.fromJson(message.json))

    }

    @Test
    fun testJsonList() {

        val messages = listOf(Message(Any::class, Fact(SomeFact("value1"))), Message(Any::class,
            Fact(SomeFact("value2"))
        ))
        assertEquals(messages, Messages.fromJson(messages.json))

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

        val messages = listOf(Message(Any::class, Fact(SomeFact("value"))), Message(Any::class, Fact(SomeOtherFact("otherValue"))))
        val instance = messages.fromJson(SomeClass::class)

        assertEquals("value", instance.someProperty)
        assertEquals("otherValue", instance.someOtherProperty)

    }

}