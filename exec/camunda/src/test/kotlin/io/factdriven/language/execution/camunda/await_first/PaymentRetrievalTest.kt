package io.factdriven.language.execution.camunda.await_first

import io.factdriven.language.Flows
import io.factdriven.execution.load
import io.factdriven.language.execution.camunda.TestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest: TestHelper() {

    init {
        Flows.initialize(
            PaymentRetrieval::class,
            CreditCardCharge::class
        )
    }

    @Test
    fun testCreditCardUnvalidated() {

        val id = send(
            PaymentRetrieval::class,
            RetrievePayment(amount = 5F)
        )
        var paymentRetrieval = PaymentRetrieval::class.load(id)
        Assertions.assertEquals(5F, paymentRetrieval.amount)
        Assertions.assertEquals(false, paymentRetrieval.retrieved)

        send(
            PaymentRetrieval::class,
            CreditCardUnvalidated()
        )
        send(
            CreditCardCharge::class,
            CreditCardGatewayConfirmationReceived(
                reference = paymentRetrieval.reference,
                amount = paymentRetrieval.amount
            )
        )

        paymentRetrieval = PaymentRetrieval::class.load(id)
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
        Assertions.assertEquals(true, paymentRetrieval.retrieved)

    }

    @Test
    fun testCreditCardValidated() {

        val id = send(
            PaymentRetrieval::class,
            RetrievePayment(amount = 5F)
        )
        var paymentRetrieval = PaymentRetrieval::class.load(id)
        Assertions.assertEquals(5F, paymentRetrieval.amount)
        Assertions.assertEquals(false, paymentRetrieval.retrieved)

        send(
            PaymentRetrieval::class,
            CreditCardValidated()
        )
        send(
            CreditCardCharge::class,
            CreditCardGatewayConfirmationReceived(
                reference = paymentRetrieval.reference,
                amount = paymentRetrieval.amount
            )
        )

        paymentRetrieval = PaymentRetrieval::class.load(id)
        Assertions.assertEquals(5F, paymentRetrieval.amount)
        Assertions.assertEquals(true, paymentRetrieval.retrieved)

    }

}