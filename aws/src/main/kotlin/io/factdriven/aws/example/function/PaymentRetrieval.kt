package io.factdriven.aws.example.function

import io.factdriven.language.define

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

    companion object {

        fun init() {

            define <PaymentRetrieval> {

                on command RetrievePayment::class

                select ("Payment (partly) uncovered?") either {
                    given ("Yes") condition { covered < total }

                } or {
                    given ("Third") // = default path w/o condition

                } or {
                    given ("No") // = default path w/o condition
                }

                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(paymentId = paymentId, payment = total)
                }

            }

        }

    }

}

data class RetrievePayment(val reference: String, val accountId: String, val payment: Float)
data class PaymentRetrievalAccepted(val paymentId: String)
data class PaymentRetrieved(val paymentId: String, val payment: Float)

