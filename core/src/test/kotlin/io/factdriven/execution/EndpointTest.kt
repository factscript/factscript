package io.factdriven.execution

import io.factdriven.definition.Definition
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
        assertEquals(Name(context="execution", local="SomeHandler"), Name.from(streamId.name))
        assertEquals(null, streamId.id)

    }

    @Test
    fun testHandling() {

        val handling = Handling(SomeFact::class)
        assertEquals(Name("execution","SomeFact"), handling.fact)
        assertEquals(emptyMap<String, Any>(), handling.details)
        assertEquals("71e98eb1ce39f2be2ae7908461ebcf98", handling.hash)

    }

    @Test
    fun testHandlingWithProperties() {

        val handling = Handling(SomeFact::class, mapOf("property" to "value"))
        assertEquals(Name("execution","SomeFact"), handling.fact)
        assertEquals(mapOf("property" to "value"), handling.details)
        assertEquals("10f95c1f34d7c81f26588b1d3ce19428", handling.hash)

    }

    @Test
    fun testEndpoint() {

        val endpoint = Handler(StreamId(SomeHandler::class), Handling(SomeFact::class))
        assertEquals(Name(context="execution", local="SomeHandler"), Name.from(endpoint.stream.name))
        assertEquals(Name(context="execution", local="SomeHandler"), Name.from(endpoint.stream.name))
        assertEquals(null, endpoint.stream.id)
        assertEquals(Name("execution","SomeFact"), endpoint.handling.fact)
        assertEquals(emptyMap<String, Any>(), endpoint.handling.details)
        assertEquals("71e98eb1ce39f2be2ae7908461ebcf98", endpoint.handling.hash)

    }

}

class MessageHandlingTest {

    @Test
    fun testHandling() {

        val definition = Definition.getDefinitionByType(PaymentRetrieval::class)
        val message = Message.from(PaymentRetrieval::class, Fact(RetrievePayment(5F)))

        val handling = definition.handling(message)
        assertEquals(1, handling.size)
        assertEquals(Name("execution", "RetrievePayment"), handling[0].fact)
        assertEquals(0, handling[0].details.size)

    }

}
