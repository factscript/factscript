package io.factdriven.flow.camunda

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

    constructor(fact: RetrievePayment): this(fact.reference, fact.accountId, fact.payment)

    var pending = 0F

    fun apply(fact: ChargeCreditCard) {

        pending = fact.charge

    }

    fun apply(fact: CreditCardCharged) {

        covered += fact.charge ?: pending

    }

    companion object {

        fun init() {

            define <PaymentRetrieval> {

                on message(RetrievePayment::class) create progress(PaymentRetrievalAccepted::class) by {
                    PaymentRetrievalAccepted(paymentId = it.reference)
                }

                execute service {
                    create intention(ChargeCreditCard::class) by { ChargeCreditCard(reference = paymentId, charge = 1F) }
                    on message(CreditCardCharged::class) having "reference" match { paymentId } create success()
                }

                execute service {
                    create intention(ChargeCreditCard::class) by { ChargeCreditCard(reference = paymentId, charge = total - covered) }
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

