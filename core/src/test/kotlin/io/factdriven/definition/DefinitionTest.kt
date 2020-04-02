package io.factdriven.definition

import io.factdriven.execution.Name
import io.factdriven.execution.name
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class DefinitionTest {

    init {
        Definitions.init(PaymentRetrieval::class)
    }

    @Test
    fun testGetDefinitionByType() {
        assertThrows<IllegalArgumentException> { Definitions.getDefinitionByType(Any::class) }
        assertDoesNotThrow { Definitions.getDefinitionByType(PaymentRetrieval::class) }
    }

    @Test
    fun testGetDefinitionByName() {
        assertThrows<IllegalArgumentException> { Definitions.getDefinitionByName(Any::class.name) }
        assertDoesNotThrow { Definitions.getDefinitionByName(Name(PaymentRetrieval::class.java.`package`.name, "PaymentRetrieval")) }
    }

}
