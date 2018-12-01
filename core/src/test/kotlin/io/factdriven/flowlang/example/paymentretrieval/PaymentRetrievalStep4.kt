package io.factdriven.flowlang.example.paymentretrieval

import io.factdriven.flowlang.execute

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow4 = execute<PaymentRetrieval> {
    on message type(RetrievePayment::class) acceptance { message ->
        PaymentRetrievalAccepted(paymentId = message.accountId)
    }
    execute service {
        create intent {
            WithdrawAmount(
                reference = status.paymentId,
                payment = status.uncovered
            )
        }
        on message type(AmountWithdrawn::class) having { "reference" to status.paymentId } success {}
    }
    select one { labeled("Payment covered?")
        given { status.uncovered > 0 } execute { labeled("No")
            execute service {
                create intent {
                    ChargeCreditCard(
                        reference = status.paymentId,
                        payment = status.uncovered
                    )
                }
                on message type(CreditCardCharged::class) having { "reference" to status.paymentId } success {}
                on message type(CreditCardExpired::class) having { "reference" to status.paymentId } mitigation {
                    execute receive {
                        on message type(CreditCardDetailsUpdated::class) having { "reference" to status.accountId } rerun {}
                        on timeout "P14D" failure {
                            PaymentFailed(status.paymentId)
                        }
                    }
                }
            }
        }
    }
    create success {
        PaymentRetrieved(status.paymentId)
    }
}
