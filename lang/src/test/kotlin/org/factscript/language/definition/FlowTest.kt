package org.factscript.language.definition

import org.factscript.execution.Type
import org.factscript.execution.type
import org.factscript.language.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class FlowTest {

    init {
        Flows.activate(FlowTestFlow::class)
    }

    @Test
    fun testGetDefinitionByType() {
        assertThrows<IllegalArgumentException> { Flows.get(Any::class) }
        assertDoesNotThrow { Flows.get(FlowTestFlow::class) }
    }

    @Test
    fun testGetDefinitionByName() {
        assertThrows<IllegalArgumentException> { Flows.get(Any::class.type) }
        assertDoesNotThrow { Flows.get(
            Type(
                FlowTestFlow::class.java.`package`.name,
                "FlowTestFlow"
            )
        ) }
    }

}

class FlowTestFlow(fact: RetrievePayment) {

    val amount = fact.amount

    companion object {

        init {
            flow <FlowTestFlow> {
                on command RetrievePayment::class
                emit event {
                    PaymentRetrieved(amount)
                }
            }
        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)