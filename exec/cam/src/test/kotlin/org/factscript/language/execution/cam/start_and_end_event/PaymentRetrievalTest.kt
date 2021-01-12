package org.factscript.language.execution.cam.start_and_end_event

import org.factscript.language.*
import org.factscript.language.execution.cam.TestHelper
import org.factscript.execution.load
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest: TestHelper() {

    init {
        Flows.activate(PaymentRetrieval::class)
    }

    @Test
    fun test() {

        val id = send(PaymentRetrieval::class, RetrievePayment(amount = 5F))
        val paymentRetrieval = PaymentRetrieval::class.load(id)
        Assertions.assertEquals(5F, paymentRetrieval.amount)
        Assertions.assertEquals(true, paymentRetrieval.retrieved)

    }

}