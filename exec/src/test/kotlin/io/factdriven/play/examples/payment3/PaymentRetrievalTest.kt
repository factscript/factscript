package io.factdriven.play.examples.payment3

import io.factdriven.def.Definition
import io.factdriven.play.PlayUsingCamundaTest
import io.factdriven.play.Player
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest: PlayUsingCamundaTest() {

    init {
        Definition.init(PaymentRetrieval::class)
        Definition.init(CreditCardCharge::class)
    }

    @Test
    fun test() {

        val id = send(PaymentRetrieval::class, RetrievePayment(amount = 5F))
        var paymentRetrieval = Player.load(id, PaymentRetrieval::class)
        Assertions.assertEquals(5F, paymentRetrieval.amount)
        Assertions.assertEquals(false, paymentRetrieval.retrieved)

        send(CreditCardCharge::class, CreditCardGatewayConfirmationReceived(reference = paymentRetrieval.reference, amount = paymentRetrieval.amount))

        paymentRetrieval = Player.load(id, PaymentRetrieval::class)
        Assertions.assertEquals(5F, paymentRetrieval.amount)
        Assertions.assertEquals(true, paymentRetrieval.retrieved)

    }

}