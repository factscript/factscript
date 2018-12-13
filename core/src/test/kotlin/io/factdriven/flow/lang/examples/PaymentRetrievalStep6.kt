package io.factdriven.flow.lang.examples

import io.factdriven.flow.execute
import io.factdriven.flow.lang.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow6 = execute<PaymentRetrieval> {
    on message RetrievePayment::class create acceptance("Payment retrieval accepted") by {
        PaymentRetrievalAccepted(paymentId = it.accountId)
    }
    execute service {
        create intent "Withdraw amount" by {
            WithdrawAmount(
                reference = paymentId,
                payment = uncovered
            )
        }
        on message AmountWithdrawn::class having "reference" match { paymentId } create success("Amount withdrawn")
        on compensation service {
            create intent "Credit amount" by {
                CreditAmount(reference = paymentId)
            }
        }
    }
    select one {
        topic("Payment covered?")
        given("No") { uncovered!! > 0 } execute service {
            create intent "Charge credit card" by {
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
                    on timeout "P14D" create failure("") by {
                        PaymentFailed(paymentId)
                    }
                }
            }
        }
    }
    create success ("Payment retrieved") by {
        PaymentRetrieved(paymentId)
    }
}
