package io.factdriven.flow.exec

import io.factdriven.flow.camunda.CamundaFlowExecutionTest
import io.factdriven.flow.lang.*
import org.apache.ibatis.logging.LogFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest: CamundaFlowExecutionTest() {

    init {
        PaymentRetrieval.init()
        CreditCardCharge.init()
    }

    // @Test
    fun testPaymentRetrieval() {

        val message = Message(RetrievePayment(reference = "anOrderId", accountId = "anAccountId", payment = 5F))

        send(message)

        val payment = find(message.id, PaymentRetrieval::class)

        assertEquals(5F, payment.covered)

    }

}
