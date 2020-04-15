package io.factdriven.aws.example.function

import io.factdriven.flow

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

    fun apply(paymentRetrievalAccepted: PaymentRetrievalAccepted){
        total += paymentRetrievalAccepted.additionalFee
    }

    var pending = 0F

    companion object {

        fun init() {

            flow<PaymentRetrieval> {

                on command RetrievePayment::class

                select ("Payment (partly) uncovered?") either {
                    given ("Yes") condition { covered < total }

                    execute command PaymentRetrievalAccepted::class by {
                        PaymentRetrievalAccepted3("1", 25f)
                    }
                } or {
                    given ("Yes") condition { covered < total }

                    execute command PaymentRetrievalAccepted::class by {
                        PaymentRetrievalAccepted3("1", 25f)
                    }

                    select ("Payment (partly) uncovered?") either {
                        given ("Yes") condition { covered < total }

                        execute command PaymentRetrievalAccepted6::class by {
                            PaymentRetrievalAccepted3("1", 25f)
                        }
                    } or {
                        given ("Yes") condition { covered < total }

                        execute command PaymentRetrievalAccepted7::class by {
                            PaymentRetrievalAccepted3("1", 25f)
                        }
                    }
                }  or {
                    given ("Yes") condition { covered < total }

                    execute command PaymentRetrievalAccepted5::class by {
                        PaymentRetrievalAccepted3("1", 25f)
                    }
                }

                execute command PaymentRetrievalAccepted::class by {
                    PaymentRetrievalAccepted("1", 25f)
                }

                execute command PaymentRetrievalAccepted2::class by {
                    PaymentRetrievalAccepted2("1", 25f)
                }

                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(paymentId = paymentId, payment = 10000f)
                }

            }

        }

    }

    fun apply(paymentRetrieved: PaymentRetrieved){
        total = paymentRetrieved.payment
    }

}

data class RetrievePayment(val reference: String, val accountId: String, val payment: Float)
data class PaymentRetrievalAccepted(val paymentId: String, val additionalFee: Float)
data class PaymentRetrievalAccepted2(val paymentId: String, val additionalFee: Float)
data class PaymentRetrievalAccepted3(val paymentId: String, val additionalFee: Float)
data class PaymentRetrievalAccepted4(val paymentId: String, val additionalFee: Float)
data class PaymentRetrievalAccepted5(val paymentId: String, val additionalFee: Float)
data class PaymentRetrievalAccepted6(val paymentId: String, val additionalFee: Float)
data class PaymentRetrievalAccepted7(val paymentId: String, val additionalFee: Float)
data class PaymentRetrieved(val paymentId: String, val payment: Float)

