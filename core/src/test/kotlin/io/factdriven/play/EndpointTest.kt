package io.factdriven.play

import io.factdriven.def.Definition
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class EndpointTest {

    data class SomeHandler(val property: String)
    data class SomeFact(val property: String)

    @Test
    fun testHandler() {

        val handler = StreamId(SomeHandler::class)
        assertEquals("SomeHandler", handler.name)
        assertEquals(null, handler.id)

    }

    @Test
    fun testHandling() {

        val handling = Handling(SomeFact::class)
        assertEquals("SomeFact", handling.fact)
        assertEquals(emptyMap<String, Any>(), handling.details)
        assertEquals("67d6e86c041f4fa48b81d1b979175cce", handling.hash)

    }

    @Test
    fun testHandlingWithProperties() {

        val handling = Handling(SomeFact::class, mapOf("property" to "value"))
        assertEquals("SomeFact", handling.fact)
        assertEquals(mapOf("property" to "value"), handling.details)
        assertEquals("8667529794db0b306f69d49632f8b346", handling.hash)

    }

    @Test
    fun testEndpoint() {

        val endpoint = Handler(StreamId(SomeHandler::class), Handling(SomeFact::class))
        assertEquals("SomeHandler", endpoint.stream.name)
        assertEquals("SomeHandler", endpoint.stream.name)
        assertEquals(null, endpoint.stream.id)
        assertEquals("SomeFact", endpoint.handling.fact)
        assertEquals(emptyMap<String, Any>(), endpoint.handling.details)
        assertEquals("67d6e86c041f4fa48b81d1b979175cce", endpoint.handling.hash)

    }

}

class MessageHandlingTest {

    @Test
    fun testHandling() {

        val definition = Definition.getDefinitionByType(PaymentRetrieval::class)
        val message = Message.from(PaymentRetrieval::class, Fact(RetrievePayment(5F)))

        val handling = definition.handling(message)
        assertEquals(1, handling.size)
        assertEquals("RetrievePayment", handling[0].fact)
        assertEquals(0, handling[0].details.size)

    }

}
