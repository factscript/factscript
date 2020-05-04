package io.factdriven.language.execution.cam.handle_failure

import io.factdriven.language.Flows
import io.factdriven.execution.load
import io.factdriven.language.execution.cam.TestHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest: TestHelper() {

    init {
        Flows.initialize(PaymentRetrieval::class, CreditCardCharge::class)
    }

    @Test
    fun test5() {

        val id = send(
            PaymentRetrieval::class,
            RetrievePayment(amount = 5F)
        )
        var paymentRetrieval = PaymentRetrieval::class.load(id)
        assertEquals(5F, paymentRetrieval.total)
        assertEquals(0F, paymentRetrieval.covered)
        assertEquals(false, paymentRetrieval.toohigh)
        assertEquals(false, paymentRetrieval.successful)
        assertEquals(false, paymentRetrieval.ended)

        send(
            CreditCardCharge::class,
            CreditCardGatewayConfirmationReceived(
                reference = paymentRetrieval.id,
                amount = paymentRetrieval.total
            )
        )

        paymentRetrieval = PaymentRetrieval::class.load(id)
        assertEquals(5F, paymentRetrieval.total)
        assertEquals(5F, paymentRetrieval.covered)
        assertEquals(false, paymentRetrieval.toohigh)
        assertEquals(true, paymentRetrieval.successful)
        assertEquals(true, paymentRetrieval.ended)

    }

    @Test
    fun test0() {

        val id = send(
            PaymentRetrieval::class,
            RetrievePayment(amount = 5F)
        )
        var paymentRetrieval = PaymentRetrieval::class.load(id)
        assertEquals(5F, paymentRetrieval.total)
        assertEquals(0F, paymentRetrieval.covered)
        assertEquals(false, paymentRetrieval.toohigh)
        assertEquals(false, paymentRetrieval.successful)
        assertEquals(false, paymentRetrieval.ended)

        send(
            CreditCardCharge::class,
            CreditCardGatewayConfirmationReceived(
                reference = paymentRetrieval.id,
                amount = 0F
            )
        )

        paymentRetrieval = PaymentRetrieval::class.load(id)
        assertEquals(5F, paymentRetrieval.total)
        assertEquals(0F, paymentRetrieval.covered)
        assertEquals(false, paymentRetrieval.toohigh)
        assertEquals(false, paymentRetrieval.successful)
        assertEquals(true, paymentRetrieval.ended)

    }

    @Test
    fun test1000() {

        val id = send(
            PaymentRetrieval::class,
            RetrievePayment(amount = 1000F)
        )
        var paymentRetrieval = PaymentRetrieval::class.load(id)
        assertEquals(1000F, paymentRetrieval.total)
        assertEquals(0F, paymentRetrieval.covered)
        assertEquals(false, paymentRetrieval.successful)
        assertEquals(false, paymentRetrieval.toohigh)
        assertEquals(false, paymentRetrieval.ended)

        send(
            CreditCardCharge::class,
            CreditCardGatewayConfirmationReceived(
                reference = paymentRetrieval.id,
                amount = 1000F
            )
        )

        paymentRetrieval = PaymentRetrieval::class.load(id)
        assertEquals(1000F, paymentRetrieval.total)
        assertEquals(0F, paymentRetrieval.covered)
        assertEquals(false, paymentRetrieval.successful)
        assertEquals(true, paymentRetrieval.ended)
        assertEquals(true, paymentRetrieval.toohigh)

    }


}