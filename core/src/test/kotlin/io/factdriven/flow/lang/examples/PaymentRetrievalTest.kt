package io.factdriven.flow.lang.examples

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun test() {

        val m = RetrievePayment(id = "id", accountId = "accountId", payment = 10F)
        val i = PaymentRetrieval(m)

        Assertions.assertEquals(m.id, i.paymentId)
        Assertions.assertEquals(m.accountId, i.accountId)

        Assertions.assertEquals(m.payment, i.uncovered)
        Assertions.assertEquals(0F, i.covered)

        i.apply(PaymentRetrieved())

        Assertions.assertEquals(0F, i.uncovered)
        Assertions.assertEquals(m.payment, i.covered)

    }

}