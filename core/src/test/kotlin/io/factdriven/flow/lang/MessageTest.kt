package io.factdriven.flow.lang

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class MessageTest {

    @Test
    fun testCreate() {

        val fact = RetrievePayment(payment = 3F, accountId = "abc")
        val message = Message.createFrom(fact)

        assertNotNull(message.id)
        assertEquals(fact::class.java.simpleName, message.name)
        assertEquals(fact, message.fact)

    }

    @Test
    fun testJson() {

        val fact = RetrievePayment(payment = 3F, accountId = "abc")
        val expected = Message.createFrom(fact)
        val json = expected.toJson()
        val actual = Message.fromJson(json, RetrievePayment::class)

        assertEquals(expected, actual)

    }

}