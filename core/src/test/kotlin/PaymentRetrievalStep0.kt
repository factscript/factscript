import io.factdriven.flowlang.execute

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow0 = execute<PaymentRetrieval> {
    on message type(RetrievePayment::class)
    execute service {
        produce intention { ChargeCreditCard() }
        on message type(CreditCardCharged::class) success {}
    }
    produce success { PaymentRetrieved() }
}
