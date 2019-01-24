package io.factdriven.flow.lang.examples

import io.factdriven.flow.execute
import io.factdriven.flow.lang.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow5 = execute<PaymentRetrieval> {
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
        on compensation service {
            create intent CreditAmount::class by {
                CreditAmount(reference = paymentId)
            }
        }
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
            on message CreditCardCharged::class having "reference" match { paymentId } create success("")
            on message CreditCardExpired::class having "reference" match { paymentId } execute mitigation {
                execute service {
                    on message CreditCardDetailsUpdated::class having "reference" match { accountId } create fix("")
                    on timeout "P14D" create failure(PaymentFailed::class) by {
                        PaymentFailed(paymentId)
                    }
                }
            }
        }
    }
    create success (PaymentRetrieved::class) by {
        PaymentRetrieved(paymentId)
    }
}
