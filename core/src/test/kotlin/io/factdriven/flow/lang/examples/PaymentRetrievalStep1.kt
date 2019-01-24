package io.factdriven.flow.lang.examples

import io.factdriven.flow.execute
import io.factdriven.flow.lang.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow1 = execute <PaymentRetrieval> ("PaymentRetrieval") {

    on message RetrievePayment::class create acceptance(PaymentRetrievalAccepted::class) by {
        PaymentRetrievalAccepted(paymentId = "paymentId")
    }

    execute service {

        create intent ChargeCreditCard::class by {
            ChargeCreditCard(
                reference = paymentId,
                payment = uncovered
            )
        }

        on message CreditCardCharged::class having "reference"  match { paymentId } create success("Credit card charged")

    }

    create success (PaymentRetrieved::class) by {
        PaymentRetrieved(paymentId)
    }

}
