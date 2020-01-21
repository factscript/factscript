package io.factdriven.play

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class EndpointTest {

    data class SomeActor(val property: String)
    data class SomeFact(val property: String)

    @Test
    fun testHandler() {

        val handler = Handler(SomeActor::class)
        assertEquals("SomeActor", handler.type)
        assertEquals(null, handler.id)
        assertEquals(null, handler.context)

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

        val endpoint = Endpoint(Handler(SomeActor::class), Handling(SomeFact::class))
        assertEquals("SomeActor", endpoint.handler.type)
        assertEquals(null, endpoint.handler.id)
        assertEquals(null, endpoint.handler.context)
        assertEquals("SomeFact", endpoint.handling.fact)
        assertEquals(emptyMap<String, Any>(), endpoint.handling.details)
        assertEquals("67d6e86c041f4fa48b81d1b979175cce", endpoint.handling.hash)

    }

}
