package io.factdriven.impl.execution

import io.factdriven.Flows
import io.factdriven.execution.*
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
        assertEquals("84be886544f2871d61992ec9969dc43b", handling.hash)

    }

    @Test
    fun testHandlingWithProperties() {

        val handling =
            Receptor(SomeFact::class, mapOf("property" to "value"))
        assertEquals(Type.from(SomeFact::class), handling.receiving)
        assertEquals(mapOf("property" to "value"), handling.expecting)
        assertEquals("0da4919659baf91c58457bd5950cc41b", handling.hash)

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
        assertEquals("84be886544f2871d61992ec9969dc43b", endpoint.receptor.hash)

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
