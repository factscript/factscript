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
        Definition.init(PaymentRetrieval::class)
    }

    @Test
    fun testGetDefinitionByType() {
        assertThrows<IllegalArgumentException> { Definition.getDefinitionByType(Any::class) }
        assertDoesNotThrow { Definition.getDefinitionByType(PaymentRetrieval::class) }
    }

    @Test
    fun testGetDefinitionByName() {
        assertThrows<IllegalArgumentException> { Definition.getDefinitionByName(Any::class.name) }
        assertDoesNotThrow { Definition.getDefinitionByName(Name(PaymentRetrieval::class.java.`package`.name, "PaymentRetrieval")) }
    }

}
