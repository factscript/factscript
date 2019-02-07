package io.factdriven.flow.lang.examples

import io.factdriven.flow.define
import io.factdriven.flow.lang.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow6 = define<PaymentRetrieval> {
    on message RetrievePayment::class create this.progress(PaymentRetrievalAccepted::class) by {
        PaymentRetrievalAccepted(paymentId = it.accountId)
    }
    execute service {
        create intention WithdrawAmount::class by {
            WithdrawAmount(
                reference = paymentId,
                payment = uncovered
            )
        }
        on message AmountWithdrawn::class having "reference" match { paymentId } create success("Amount withdrawn")
        on compensation service {
            create intention CreditAmount::class by {
                CreditAmount(reference = paymentId)
            }
        }
    }
    select one {
        topic("Payment covered?")
        given("No") { uncovered!! > 0 } execute service {
            create intention ChargeCreditCard::class by {
                ChargeCreditCard(
                    reference = paymentId,
                    payment = uncovered
                )
            }
            on message CreditCardCharged::class having "reference" match { paymentId } create success("")
            on message CreditCardExpired::class having "reference" match { paymentId } execute mitigation {
                execute service {
                    on message CreditCardDetailsUpdated::class having "reference" match { accountId } create fix("")
                    on message PaymentCoveredManually::class having "reference" match { accountId } create success("")
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
