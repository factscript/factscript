package io.factdriven.impl.execution.examples.payment4

import io.factdriven.Flows
import io.factdriven.execution.load
import io.factdriven.impl.execution.PlayUsingCamundaTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest: PlayUsingCamundaTest() {

    init {
        Flows.initialize(
            PaymentRetrieval::class,
            CreditCardCharge::class
        )
    }

    @Test
    fun testPositiveAmount() {

        val id = send(PaymentRetrieval::class, RetrievePayment(amount = 5F))
        var paymentRetrieval = PaymentRetrieval::class.load(id)
        Assertions.assertEquals(5F, paymentRetrieval.amount)
        Assertions.assertEquals(false, paymentRetrieval.retrieved)

        send(CreditCardCharge::class, CreditCardGatewayConfirmationReceived(reference = paymentRetrieval.reference, amount = paymentRetrieval.amount))

        paymentRetrieval = PaymentRetrieval::class.load(id)
        Assertions.assertEquals(5F, paymentRetrieval.amount)
        Assertions.assertEquals(true, paymentRetrieval.retrieved)

    }

    @Test
    fun testZeroAmount() {

        val id = send(PaymentRetrieval::class, RetrievePayment(amount = 0F))
        val paymentRetrieval = PaymentRetrieval::class.load(id)
        Assertions.assertEquals(0F, paymentRetrieval.amount)
        Assertions.assertEquals(true, paymentRetrieval.retrieved)

    }

}