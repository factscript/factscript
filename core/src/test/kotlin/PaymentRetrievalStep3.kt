import io.factdriven.flowlang.execute

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow3 = execute<PaymentRetrieval> {
    on message type(RetrievePayment::class) milestone { message ->
        PaymentRetrievalAccepted(paymentId = message.accountId)
    }
    execute service {
        produce intention {
            WithdrawAmount(reference = status.paymentId, payment = status.uncovered)
        }
        on message type(AmountWithdrawn::class) having { "reference" to status.paymentId } success {}
    }
    select one { labeled("Payment covered?")
        given { status.uncovered > 0 } execute { labeled("No")
            execute service {
                produce intention {
                    ChargeCreditCard(reference = status.paymentId, payment = status.uncovered)
                }
                on message type(CreditCardCharged::class) having { "reference" to status.paymentId } success {}
                on message type(CreditCardExpired::class) having { "reference" to status.paymentId } mitigation {
                    on message type(CreditCardDetailsUpdated::class) having { "reference" to status.paymentId } retry {}
                }
            }
        }
    }
    produce success {
        PaymentRetrieved(status.paymentId)
    }
}
