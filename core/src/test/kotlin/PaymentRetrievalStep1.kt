import io.factdriven.flowlang.execute

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow1 = execute<PaymentRetrieval> {
    on message type(RetrievePayment::class) milestone { message ->
        PaymentRetrievalAccepted(paymentId = message.accountId)
    }
    execute service {
        produce intent {
            ChargeCreditCard(reference = status.paymentId, payment = status.uncovered)
        }
        on message type(CreditCardCharged::class) having { "reference" to status.paymentId } success {}
    }
    produce success {
        PaymentRetrieved(status.paymentId)
    }
}
