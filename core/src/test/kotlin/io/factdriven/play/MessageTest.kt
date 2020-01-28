package io.factdriven.play

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
        val message = Message(fact)
        assertNotNull(message.id)
        assertEquals(fact, message.fact)

    }

    @Test
    fun testJson() {

        val message = Message(Fact(SomeFact("value")))
        assertNotNull(message.toJson())
        assertEquals(message, Message.fromJson(message.toJson()))

    }

    @Test
    fun testJsonList() {

        val messages = listOf(Message(Fact(SomeFact("value1"))), Message(
            Fact(SomeFact("value2"))
        ))
        assertEquals(messages, Message.list.fromJson(messages.toJson()))

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

        val messages = listOf(Message(Fact(SomeFact("value"))), Message(Fact(SomeOtherFact("otherValue"))))
        val instance = messages.applyTo(SomeClass::class)

        assertEquals("value", instance.someProperty)
        assertEquals("otherValue", instance.someOtherProperty)

    }

}