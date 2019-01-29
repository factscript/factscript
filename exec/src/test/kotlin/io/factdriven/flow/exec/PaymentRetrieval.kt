package io.factdriven.flow.exec

import io.factdriven.flow.define

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class PaymentRetrieval(

    val paymentId: String?,
    val accountId: String?,
    var uncovered: Float,
    var covered: Float

) {

    constructor(command: RetrievePayment): this(command.reference, command.accountId, command.payment, 0F)

    fun apply(event: PaymentRetrieved) {

        covered += event.payment ?: uncovered
        uncovered -= event.payment ?: uncovered

    }

    companion object {

        fun init() {

            define <PaymentRetrieval> {

                on message(RetrievePayment::class) create acceptance(PaymentRetrievalAccepted::class) by {
                    PaymentRetrievalAccepted(paymentId = it.reference)
                }

                execute service {
                    create intent(ChargeCreditCard::class) by { ChargeCreditCard(reference = paymentId, payment = uncovered) }
                    on message(CreditCardCharged::class) having "reference" match { paymentId } create success()
                }

                create success(PaymentRetrieved::class) by {
                    PaymentRetrieved(paymentId = paymentId, payment = uncovered)
                }

            }

        }

    }

}

data class RetrievePayment(val reference: String? = null, val accountId: String? = null, val payment: Float)
data class PaymentRetrievalAccepted(val paymentId: String? = null)
data class PaymentRetrieved(val paymentId: String? = null, val payment: Float? = null)

data class ChargeCreditCard(val reference: String? = null, val payment: Float? = null)
data class CreditCardCharged(val reference: String? = null)
