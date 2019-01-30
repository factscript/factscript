package io.factdriven.flow.exec

import io.factdriven.flow.define

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class PaymentRetrieval(

    val paymentId: String,
    val accountId: String,
    var total: Float,
    var covered: Float = 0F

) {

    constructor(command: RetrievePayment): this(command.reference, command.accountId, command.payment)

    var pending = 0F

    fun apply(command: ChargeCreditCard) {

        pending = command.charge!!

    }

    fun apply(event: CreditCardCharged) {

        covered += event.charge ?: pending

    }

    companion object {

        fun init() {

            define <PaymentRetrieval> {

                on message(RetrievePayment::class) create progress(PaymentRetrievalAccepted::class) by {
                    PaymentRetrievalAccepted(paymentId = it.reference)
                }

                execute service {
                    create intent(ChargeCreditCard::class) by { ChargeCreditCard(reference = paymentId, charge = total ) }
                    on message(CreditCardCharged::class) having "reference" match { paymentId } create success()
                }

                create success(PaymentRetrieved::class) by {
                    PaymentRetrieved(paymentId = paymentId, payment = total)
                }

            }

        }

    }

}

data class RetrievePayment(val reference: String, val accountId: String, val payment: Float)
data class PaymentRetrievalAccepted(val paymentId: String)
data class PaymentRetrieved(val paymentId: String, val payment: Float)

