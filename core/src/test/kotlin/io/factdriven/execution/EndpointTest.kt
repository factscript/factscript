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
        assertEquals(Name(SomeHandler::class.java.`package`.name, local="SomeHandler"), Name.from(streamId.name))
        assertEquals(null, streamId.id)

    }

    @Test
    fun testHandling() {

        val handling = Handling(SomeFact::class)
        assertEquals(Name(SomeFact::class.java.`package`.name,"SomeFact"), handling.fact)
        assertEquals(emptyMap<String, Any>(), handling.details)
        assertEquals("7bcd0970753f751ef3eb18181c3447c6", handling.hash)

    }

    @Test
    fun testHandlingWithProperties() {

        val handling = Handling(SomeFact::class, mapOf("property" to "value"))
        assertEquals(Name(SomeFact::class.java.`package`.name,"SomeFact"), handling.fact)
        assertEquals(mapOf("property" to "value"), handling.details)
        assertEquals("4e5675bf410e9dc7e67513f0803a7c87", handling.hash)

    }

    @Test
    fun testEndpoint() {

        val endpoint = Handler(StreamId(SomeHandler::class), Handling(SomeFact::class))
        assertEquals(Name(SomeHandler::class.java.`package`.name, local="SomeHandler"), Name.from(endpoint.stream.name))
        assertEquals(Name(SomeHandler::class.java.`package`.name, local="SomeHandler"), Name.from(endpoint.stream.name))
        assertEquals(null, endpoint.stream.id)
        assertEquals(Name(SomeFact::class.java.`package`.name,"SomeFact"), endpoint.handling.fact)
        assertEquals(emptyMap<String, Any>(), endpoint.handling.details)
        assertEquals("7bcd0970753f751ef3eb18181c3447c6", endpoint.handling.hash)

    }

}

class MessageHandlingTest {

    @Test
    fun testHandling() {

        val definition = Definition.getDefinitionByType(PaymentRetrieval::class)
        val message = Message.from(PaymentRetrieval::class, Fact(RetrievePayment(5F)))

        val handling = definition.handling(message)
        assertEquals(1, handling.size)
        assertEquals(Name(RetrievePayment::class.java.`package`.name, "RetrievePayment"), handling[0].fact)
        assertEquals(0, handling[0].details.size)

    }

}
