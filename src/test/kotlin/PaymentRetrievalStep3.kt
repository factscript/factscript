import io.factdriven.flowlang.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow3 = flow<PaymentRetrieval> {
    on message type(RetrievePayment::class) progress { message ->
        PaymentRetrievalAccepted(paymentId = message.accountId)
    }
    perform service {
        create intent {
            WithdrawAmount(reference = flow.paymentId, payment = flow.uncovered)
        }
        on message type(AmountWithdrawn::class) having { "reference" to flow.paymentId } success {}
    }
    choose one { labeled("Payment covered?")
        given { flow.uncovered > 0 } flow { labeled("No")
            perform service {
                create intent {
                    ChargeCreditCard(reference = flow.paymentId, payment = flow.uncovered)
                }
                on message type(CreditCardCharged::class) having { "reference" to flow.paymentId } success {}
                on message type(CreditCardExpired::class) having { "reference" to flow.paymentId } mitigate {
                    on message type(CreditCardDetailsUpdated::class) having { "reference" to flow.paymentId } retry {}
                }
            }
        }
    }
    create success {
        PaymentRetrieved(flow.paymentId)
    }
}
