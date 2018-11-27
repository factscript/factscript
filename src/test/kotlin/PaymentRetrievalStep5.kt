import io.factdriven.flowlang.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow5 = flow<PaymentRetrieval> {
    on message type(RetrievePayment::class) progress { message ->
        PaymentRetrievalAccepted(paymentId = message.accountId)
    }
    perform service {
        create intent {
            WithdrawAmount(reference = flow.paymentId, payment = flow.uncovered)
        }
        on message type(AmountWithdrawn::class) having { "reference" to flow.paymentId } success {}
        on compensation {
            create intent {
                CreditAmount(reference = flow.paymentId)
            }
        }
    }
    choose one { labeled("Payment covered?")
        given { flow.uncovered > 0 } flow { labeled("No")
            perform service {
                create intent {
                    ChargeCreditCard(reference = flow.paymentId, payment = flow.uncovered)
                }
                on message type(CreditCardCharged::class) having { "reference" to flow.paymentId } success {}
                on message type(CreditCardExpired::class) having { "reference" to flow.paymentId } mitigate {
                    perform receive {
                        on message type(CreditCardDetailsUpdated::class) having { "reference" to flow.accountId } retry {}
                        on timeout "P14D" failure {
                            PaymentFailed(flow.paymentId)
                        }
                    }
                }
            }
        }
    }
    create success {
        PaymentRetrieved(flow.paymentId)
    }
}
