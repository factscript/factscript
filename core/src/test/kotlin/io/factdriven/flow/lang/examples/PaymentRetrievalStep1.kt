package io.factdriven.flow.lang.examples

import io.factdriven.flow.define
import io.factdriven.flow.lang.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow1 = define <PaymentRetrieval> ("PaymentRetrieval") {

    on message RetrievePayment::class create this.progress(PaymentRetrievalAccepted::class) by {
        PaymentRetrievalAccepted(paymentId = "paymentId")
    }

    execute service {

        create intention ChargeCreditCard::class by {
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
