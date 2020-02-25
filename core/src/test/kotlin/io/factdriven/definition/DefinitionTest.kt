package io.factdriven.definition

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class DefinitionTest {

    init {
        Definition.init(PaymentRetrieval::class)
    }

    @Test
    fun testGetDefinitionByType() {
        assertThrows<IllegalArgumentException> { Definition.getDefinitionByType(Any::class) }
        assertDoesNotThrow { Definition.getDefinitionByType(PaymentRetrieval::class) }
    }

    @Test
    fun testGetDefinitionById() {
        assertThrows<IllegalArgumentException> { Definition.getDefinitionById("Any") }
        assertDoesNotThrow { Definition.getDefinitionById("PaymentRetrieval") }
        assertDoesNotThrow { Definition.getDefinitionById("PaymentRetrieval-Any") }
    }

    @Test
    fun testGetNodeById() {
        assertThrows<IllegalArgumentException> { Definition.getNodeById("Any") }
        assertThrows<IllegalArgumentException> { Definition.getNodeById("PaymentRetrieval-RetrievePayment-0") }
        assertTrue(Definition.getNodeById("PaymentRetrieval-RetrievePayment-1") is Consuming)
        assertTrue(Definition.getNodeById("PaymentRetrieval-PaymentRetrieved-1") is Throwing)
    }

}
