package org.factscript.language.execution.cam.select_all

import org.factscript.language.*
import org.factscript.execution.load
import org.factscript.language.execution.cam.TestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest: TestHelper() {

    init {
        Flows.activate(PaymentRetrieval::class, CreditCardCharge::class)
    }

    @Test
    fun test5() {

        val id = send(
            PaymentRetrieval::class,
            RetrievePayment(amount = 5F)
        )
        var paymentRetrieval = PaymentRetrieval::class.load(id)
        Assertions.assertEquals(5F, paymentRetrieval.amount)
        Assertions.assertEquals(0F, paymentRetrieval.charged)
        Assertions.assertEquals(false, paymentRetrieval.retrieved)

        send(
            CreditCardCharge::class,
            CreditCardGatewayConfirmationReceived(
                reference = paymentRetrieval.reference,
                amount = paymentRetrieval.amount
            )
        )

        paymentRetrieval = PaymentRetrieval::class.load(id)
        Assertions.assertEquals(5F, paymentRetrieval.amount)
        Assertions.assertEquals(5F, paymentRetrieval.charged)
        Assertions.assertEquals(true, paymentRetrieval.retrieved)

    }

    @Test
    fun test20() {

        val id = send(
            PaymentRetrieval::class,
            RetrievePayment(amount = 20F)
        )
        var paymentRetrieval = PaymentRetrieval::class.load(id)
        Assertions.assertEquals(20F, paymentRetrieval.amount)
        Assertions.assertEquals(0F, paymentRetrieval.charged)
        Assertions.assertEquals(false, paymentRetrieval.retrieved)

        send(
            CreditCardCharge::class,
            CreditCardGatewayConfirmationReceived(
                reference = paymentRetrieval.reference,
                amount = paymentRetrieval.amount
            )
        )

        paymentRetrieval = PaymentRetrieval::class.load(id)
        Assertions.assertEquals(20F, paymentRetrieval.amount)
        Assertions.assertEquals(20F, paymentRetrieval.charged)
        Assertions.assertEquals(true, paymentRetrieval.retrieved)

    }

    @Test
    fun test0() {

        val id = send(
            PaymentRetrieval::class,
            RetrievePayment(amount = 0F)
        )
        val paymentRetrieval = PaymentRetrieval::class.load(id)
        Assertions.assertEquals(0F, paymentRetrieval.amount)
        Assertions.assertEquals(0F, paymentRetrieval.charged)
        Assertions.assertEquals(true, paymentRetrieval.retrieved)

    }

}