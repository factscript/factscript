package io.factdriven.flow.lang.examples

import io.factdriven.flow.execute
import io.factdriven.flow.lang.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow1 = execute<PaymentRetrieval>("Payment retrieval") {

    on message RetrievePayment::class create acceptance("Payment retrieval accepted") by {
        PaymentRetrievalAccepted(paymentId = it.id)
    }

    execute service {

        create intent "Charge credit card" by {
            ChargeCreditCard(
                reference = paymentId,
                payment = uncovered
            )
        }

        on message CreditCardCharged::class having "reference"  match { paymentId } create success("Credit card charged")

    }

    create success ("Payment retrieved") by {
        PaymentRetrieved(paymentId)
    }

}
