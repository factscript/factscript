package io.factdriven.flow.lang

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class DefaultFlowMessagePatternTest {

    @Test
    fun testTypeMatching() {

        val pattern = DefaultFlowMessagePattern(RetrievePayment::class)
        val message = RetrievePayment()

        assertTrue(pattern.matches(message))

    }

    @Test
    fun testTypeNotMatching() {

        val pattern = DefaultFlowMessagePattern(RetrievePayment::class)
        val message = PaymentRetrieved()

        assertFalse(pattern.matches(message))

    }

    @Test
    fun testTypeAndOneKeyMatching() {

        val pattern = DefaultFlowMessagePattern(RetrievePayment::class)
        pattern.add("id" to "123")

        val message = RetrievePayment(id = "123")

        assertTrue(pattern.matches(message))

    }

    @Test
    fun testTypeAndOneKeyNotMatching() {

        val pattern = DefaultFlowMessagePattern(RetrievePayment::class)
        pattern.add("id" to "123")

        val message = RetrievePayment(id = "321")

        assertFalse(pattern.matches(message))

    }

    @Test
    fun testTypeAndTwoKeysMatching() {

        val pattern = DefaultFlowMessagePattern(RetrievePayment::class)
        pattern.add("id" to "123")
        pattern.add("accountId" to "456")

        val message = RetrievePayment(id = "123", accountId = "456")

        assertTrue(pattern.matches(message))

    }

    @Test
    fun testTypeAndOneOfTwoKeysNotMatching() {

        val pattern = DefaultFlowMessagePattern(RetrievePayment::class)
        pattern.add("id" to "123")
        pattern.add("accountId" to "456")

        val message = RetrievePayment(id = "321", accountId = "456")

        assertFalse(pattern.matches(message))

    }

    @Test
    fun testTypeWithMoreKeysAndOneKeyMatching() {

        val pattern = DefaultFlowMessagePattern(RetrievePayment::class)
        pattern.add("id" to "123")

        val message = RetrievePayment(id = "123", accountId = "456")

        assertTrue(pattern.matches(message))

    }

    @Test
    fun testTypeWithMoreKeysAndOneKeyNotMatching() {

        val pattern = DefaultFlowMessagePattern(RetrievePayment::class)
        pattern.add("id" to "321")

        val message = RetrievePayment(id = "123", accountId = "456")

        assertFalse(pattern.matches(message))

    }

    @Test
    fun testTypeWithMoreKeysAndOneValueMissing() {

        val pattern = DefaultFlowMessagePattern(RetrievePayment::class)
        pattern.add("id" to "123")
        pattern.add("accountId" to "456")

        val message = RetrievePayment(id = "123")

        assertFalse(pattern.matches(message))

    }

    @Test
    fun testTypeWithMoreKeysAndOneKeyMissing() {

        val pattern = DefaultFlowMessagePattern(RetrievePayment::class)
        pattern.add("id" to "123")
        pattern.add("missingKey" to "456")

        val message = RetrievePayment(id = "123")

        assertFalse(pattern.matches(message))

    }

}