import io.factdriven.flowlang.execute

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow0 = execute<PaymentRetrieval> {
    on message type(RetrievePayment::class) milestone {
        PaymentRetrievalAccepted()
    }
    execute service {
        produce intent {
            ChargeCreditCard()
        }
        on message type(CreditCardCharged::class) success {}
    }
    produce success {
        PaymentRetrieved(status.paymentId)
    }
}
