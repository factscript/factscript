package io.factdriven.flow.lang.examples

import io.factdriven.flow.*
import io.factdriven.flow.lang.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow2 = execute<PaymentRetrieval> {
    on message RetrievePayment::class create acceptance(PaymentRetrievalAccepted::class) by {
        PaymentRetrievalAccepted(paymentId = it.accountId)
    }
    execute service {
        create intent WithdrawAmount::class by {
            WithdrawAmount(
                reference = paymentId,
                payment = uncovered
            )
        }
        on message AmountWithdrawn::class having "reference" match { paymentId } create success("Amount withdrawn")
    }
    select one {
        topic("Payment covered?")
        given("No") { uncovered!! > 0 } execute service {
            create intent ChargeCreditCard::class by {
                ChargeCreditCard(
                    reference = paymentId,
                    payment = uncovered
                )
            }
            on message CreditCardCharged::class having "reference" match { paymentId } create success("Credit card charged")
        }
    }
    create success (PaymentRetrieved::class) by {
        PaymentRetrieved(paymentId)
    }
}
