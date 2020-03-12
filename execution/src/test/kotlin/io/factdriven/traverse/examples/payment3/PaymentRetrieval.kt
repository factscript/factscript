package io.factdriven.traverse.examples.payment3

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

        init {

            define <PaymentRetrieval> {

                on command RetrievePayment::class

                execute command PaymentRetrieved::class by {
                    PaymentRetrieved(paymentId = paymentId, payment = total)
                }

                select ("Payment (partly) uncovered?") either {
                    given ("Yes") condition { covered < total }

                    select ("Nested Payment (partly) uncovered?") either {
                        given ("Kleiner") condition { covered < total }

                        execute command PaymentRetrieved3::class by {
                            PaymentRetrieved3(paymentId = paymentId, payment = total)
                        }
                    } or {
                        given ("GroesserGleich") // = default path w/o condition

                        execute command PaymentRetrieved4::class by {
                            PaymentRetrieved4(paymentId = paymentId, payment = total)
                        }
                    }
                } or {
                    given ("No") // = default path w/o condition

                     execute command PaymentRetrieved2::class by {
                         PaymentRetrieved2(paymentId = paymentId, payment = total)
                     }
                }

                emit event PaymentRetrievedFin::class by {
                    PaymentRetrievedFin(paymentId = paymentId, payment = total)
                }
            }

        }

    }

}

data class RetrievePayment(val reference: String, val accountId: String, val payment: Float)
data class PaymentRetrievalAccepted(val paymentId: String)
data class PaymentRetrieved(val paymentId: String, val payment: Float)
data class PaymentRetrieved1(val paymentId: String, val payment: Float)
data class PaymentRetrieved2(val paymentId: String, val payment: Float)
data class PaymentRetrieved3(val paymentId: String, val payment: Float)
data class PaymentRetrieved4(val paymentId: String, val payment: Float)
data class PaymentRetrievedFin(val paymentId: String, val payment: Float)

