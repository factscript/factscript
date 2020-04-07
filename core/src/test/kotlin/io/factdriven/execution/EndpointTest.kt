package io.factdriven.execution

import io.factdriven.definition.Flows
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

        val streamId = StreamId(SomeHandler::class)
        assertEquals(Type.from(SomeHandler::class), Type.from(streamId.name))
        assertEquals(null, streamId.id)

    }

    @Test
    fun testHandling() {

        val handling = Handling(SomeFact::class)
        assertEquals(Type.from(SomeFact::class), handling.fact)
        assertEquals(emptyMap<String, Any>(), handling.details)
        assertEquals("0fa0565b21d4b135e15ed3cb61bfebfe", handling.hash)

    }

    @Test
    fun testHandlingWithProperties() {

        val handling = Handling(SomeFact::class, mapOf("property" to "value"))
        assertEquals(Type.from(SomeFact::class), handling.fact)
        assertEquals(mapOf("property" to "value"), handling.details)
        assertEquals("992044e9fb7d4e7eafdff46cd8757b07", handling.hash)

    }

    @Test
    fun testEndpoint() {

        val endpoint = Handler(StreamId(SomeHandler::class), Handling(SomeFact::class))
        assertEquals(Type.from(SomeHandler::class), Type.from(endpoint.stream.name))
        assertEquals(Type.from(SomeHandler::class), Type.from(endpoint.stream.name))
        assertEquals(null, endpoint.stream.id)
        assertEquals(Type.from(SomeFact::class), endpoint.handling.fact)
        assertEquals(emptyMap<String, Any>(), endpoint.handling.details)
        assertEquals("0fa0565b21d4b135e15ed3cb61bfebfe", endpoint.handling.hash)

    }

}

class MessageHandlingTest {

    @Test
    fun testHandling() {

        val definition = Flows.findByClass(PaymentRetrieval::class)
        val message = Message.from(PaymentRetrieval::class, Fact(RetrievePayment(5F)))

        val handling = definition.handling(message)
        assertEquals(1, handling.size)
        assertEquals(Type(RetrievePayment::class.java.`package`.name, "RetrievePayment"), handling[0].fact)
        assertEquals(0, handling[0].details.size)

    }

}
