package io.factdriven.execution.examples.payment1

import io.factdriven.definition.Definition
import io.factdriven.definition.Definitions
import io.factdriven.execution.PlayUsingCamundaTest
import io.factdriven.execution.Player
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest: PlayUsingCamundaTest() {

    init {
        Definitions.clear()
        Definitions.init(PaymentRetrieval::class)
    }

    @Test
    fun test() {

        val id = send(PaymentRetrieval::class, RetrievePayment(amount = 5F))
        val paymentRetrieval = Player.load(id, PaymentRetrieval::class)
        Assertions.assertEquals(5F, paymentRetrieval.amount)
        Assertions.assertEquals(true, paymentRetrieval.retrieved)

    }

}