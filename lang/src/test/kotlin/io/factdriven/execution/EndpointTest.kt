package io.factdriven.execution

import io.factdriven.language.Flows
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

        val streamId = EntityId(SomeHandler::class)
        assertEquals(Type.from(SomeHandler::class), streamId.type)
        assertEquals(null, streamId.id)

    }

    @Test
    fun testHandling() {

        val handling = Receptor(SomeFact::class)
        assertEquals(Type.from(SomeFact::class), handling.receiving)
        assertEquals(emptyMap<String, Any>(), handling.expecting)
        assertEquals("gid-ca55d68d-beb5-3077-988e-fc3a373e066b", handling.hash)

    }

    @Test
    fun testHandlingWithProperties() {

        val handling =
            Receptor(SomeFact::class, mapOf("property" to "value"))
        assertEquals(Type.from(SomeFact::class), handling.receiving)
        assertEquals(mapOf("property" to "value"), handling.expecting)
        assertEquals("gid-6cdfcf47-59d3-3e3f-af97-fb8668c76e4a", handling.hash)

    }

    @Test
    fun testEndpoint() {

        val endpoint = Receiver(
            EntityId(SomeHandler::class),
            Receptor(SomeFact::class)
        )
        assertEquals(Type.from(SomeHandler::class), endpoint.entity.type)
        assertEquals(Type.from(SomeHandler::class), endpoint.entity.type)
        assertEquals(null, endpoint.entity.id)
        assertEquals(Type.from(SomeFact::class), endpoint.receptor.receiving)
        assertEquals(emptyMap<String, Any>(), endpoint.receptor.expecting)
        assertEquals("gid-ca55d68d-beb5-3077-988e-fc3a373e066b", endpoint.receptor.hash)

    }

}

class MessageReceptorTest {

    @Test
    fun testHandling() {

        val definition = Flows.get(PaymentRetrieval::class)
        val message = Message(
            PaymentRetrieval::class,
            Fact(RetrievePayment(5F))
        )

        val handling = definition.findReceptorsFor(message)
        assertEquals(1, handling.size)
        assertEquals(
            Type(
                RetrievePayment::class.java.`package`.name,
                "RetrievePayment"
            ), handling[0].receiving)
        assertEquals(0, handling[0].expecting.size)

    }

}
