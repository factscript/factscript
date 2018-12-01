import io.factdriven.flowlang.execute

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
val flow0 = execute<PaymentRetrieval> {
    on message type(RetrievePayment::class)
    execute service {
        create intent { ChargeCreditCard() }
        on message type(CreditCardCharged::class) success {}
    }
    create success { PaymentRetrieved() }
}
