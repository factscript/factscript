package io.factdriven.flow.lang.examples

import io.factdriven.flow.lang.execute

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow1 = execute<PaymentRetrieval>("Payment retrieval") {
    on message type(RetrievePayment::class) create acceptance("Payment retrieval accepted") by { message ->
        PaymentRetrievalAccepted(paymentId = message.id)
    }
    execute service {
        create intent ("Charge credit card") by {
            ChargeCreditCard(
                reference = status.paymentId,
                payment = status.uncovered
            )
        }
        on message type(CreditCardCharged::class) having { "reference" to status.paymentId } create success("Credit card charged")
    }
    create success ("Payment retrieved") by {
        PaymentRetrieved(status.paymentId)
    }
}
