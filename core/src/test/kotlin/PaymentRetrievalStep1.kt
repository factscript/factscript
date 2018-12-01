import io.factdriven.flowlang.execute

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow1 = execute<PaymentRetrieval> {
    on message type(RetrievePayment::class) acceptance { message ->
        PaymentRetrievalAccepted(paymentId = message.id)
    }
    execute service {
        create intent {
            ChargeCreditCard(reference = status.paymentId, payment = status.uncovered)
        }
        on message type(CreditCardCharged::class) having { "reference" to status.paymentId } success {}
    }
    create success {
        PaymentRetrieved(status.paymentId)
    }
}
