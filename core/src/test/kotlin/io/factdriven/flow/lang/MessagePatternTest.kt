package io.factdriven.flow.lang

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class MessagePatternTest {

    @Test
    fun testFlowMessagePatternType() {

        val pattern1 = MessagePattern(PaymentRetrieval::class, type = PaymentRetrieved::class)
        val pattern2 = MessagePattern(PaymentRetrieval::class, type = PaymentRetrieved::class)
        val pattern3 = MessagePattern(PaymentRetrieval::class, type = PaymentFailed::class)

        assertEquals(pattern1, pattern2)
        assertEquals(pattern1.hash, pattern2.hash)

        assertNotEquals(pattern1, pattern3)
        assertNotEquals(pattern1.hash, pattern3.hash)

    }

    @Test
    fun testFlowMessagePatternTypeWithOneKey() {

        val pattern1 = MessagePattern(PaymentRetrieval::class, type = PaymentRetrieved::class, properties = mapOf("key" to 123))
        val pattern2 = MessagePattern(PaymentRetrieval::class, type = PaymentRetrieved::class, properties = mapOf("key" to "123"))
        val pattern3 = MessagePattern(PaymentRetrieval::class, type = PaymentRetrieved::class, properties = mapOf("key" to "321"))

        assertEquals(pattern1, pattern2)
        assertEquals(pattern1.hash, pattern2.hash)
        assertNotEquals(pattern1.hash, pattern3.hash)

    }

    @Test
    fun testFlowMessagePatternTypeWithTwoKeys() {

        val pattern1 = MessagePattern(PaymentRetrieval::class, type = PaymentRetrieved::class, properties = mapOf("key1" to 123, "key2" to "321"))
        val pattern2 = MessagePattern(PaymentRetrieval::class, type = PaymentRetrieved::class, properties = mapOf("key2" to "321", "key1" to 123))
        val pattern3 = MessagePattern(PaymentRetrieval::class, type = PaymentRetrieved::class, properties = mapOf("key2" to "123", "key1" to 321))

        assertEquals(pattern1, pattern2)
        assertEquals(pattern1.hash, pattern2.hash)

        assertNotEquals(pattern1, pattern3)
        assertNotEquals(pattern1.hash, pattern3.hash)

    }

}