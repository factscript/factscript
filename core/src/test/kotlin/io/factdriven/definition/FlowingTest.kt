package io.factdriven.definition

import io.factdriven.execution.Type
import io.factdriven.execution.type
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class FlowingTest {

    init {
        Flows.init(PaymentRetrieval::class)
    }

    @Test
    fun testGetDefinitionByType() {
        assertThrows<IllegalArgumentException> { Flows.getDefinitionByType(Any::class) }
        assertDoesNotThrow { Flows.getDefinitionByType(PaymentRetrieval::class) }
    }

    @Test
    fun testGetDefinitionByName() {
        assertThrows<IllegalArgumentException> { Flows.getDefinitionByName(Any::class.type) }
        assertDoesNotThrow { Flows.getDefinitionByName(Type(PaymentRetrieval::class.java.`package`.name, "PaymentRetrieval")) }
    }

}
