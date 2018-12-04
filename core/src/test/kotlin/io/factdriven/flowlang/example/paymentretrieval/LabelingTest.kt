package io.factdriven.flowlang.example.paymentretrieval

import io.factdriven.flowlang.name
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class LabelingTest {

    @Test
    fun testLabeling() {
        val test = RetrievePayment::class
        assertEquals("Retrieve payment", name(test))
    }

}