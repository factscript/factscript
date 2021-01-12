package org.factscript.execution

import org.factscript.language.*
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
        assertEquals("gid-18d5ad17-69bd-34e5-90b0-9bca75b63adb", handling.hash)

    }

    @Test
    fun testHandlingWithProperties() {

        val handling =
            Receptor(SomeFact::class, mapOf("property" to "value"))
        assertEquals(Type.from(SomeFact::class), handling.receiving)
        assertEquals(mapOf("property" to "value"), handling.expecting)
        assertEquals("gid-072451b9-355c-3792-a40b-7667514a324a", handling.hash)

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
        assertEquals("gid-18d5ad17-69bd-34e5-90b0-9bca75b63adb", endpoint.receptor.hash)

    }

}

class MessageReceptorTest {

    init { Flows.activate(PaymentRetrieval::class) }

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
            ), handling.iterator().next().receiving)
        assertEquals(0, handling.iterator().next().expecting.size)

    }

}
