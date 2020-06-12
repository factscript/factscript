package io.factdriven.language.execution.aws.example.function

import io.factdriven.language.*

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

//                select all {
//                    given ("Yes") condition { true }
//
//                    execute command {
//                        PaymentRetrievalAccepted("1", 25f)
//                    }
//                } or {
//                    given ("Yes") condition { true }
//
//                    execute command {
//                        PaymentRetrievalAccepted3("1", 25f)
//                    }
//                }

//                execute all {
//                    execute command {
//                        PaymentRetrievalAccepted("1", 25f)
//                    }
//
//                } and {
//                    execute command {
//                        PaymentRetrievalAccepted2("1", 25f)
//                    }
//
//                    execute all {
//                        execute command {
//                            PaymentRetrievalAccepted3("1", 25f)
//                        }
//
//                    } and {
//                        execute command {
//                            PaymentRetrievalAccepted4("1", 25f)
//                        }
//                    } and {
//                        execute command {
//                            PaymentRetrievalAccepted5("1", 25f)
//                        }
//
//                        execute all {
//                            execute command {
//                                PaymentRetrievalAccepted3("1", 25f)
//                            }
//
//                        } and {
//                            execute command {
//                                PaymentRetrievalAccepted4("1", 25f)
//                            }
//                        } and {
//                            execute command {
//                                PaymentRetrievalAccepted5("1", 25f)
//                            }
//
//                            select either {
//                                given("Yes") condition { true }
//
//                                execute command {
//                                    PaymentRetrievalAccepted("1", 25f)
//                                }
//                            } or {
//                                given("Yes") condition { true }
//
//                                execute command {
//                                    PaymentRetrievalAccepted3("1", 25f)
//                                }
//
//                                execute all {
//                                    execute command {
//                                        PaymentRetrievalAccepted3("1", 25f)
//                                    }
//
//                                } and {
//                                    execute command {
//                                        PaymentRetrievalAccepted4("1", 25f)
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }

                emit event {
                    ChargeCreditCard(reference = "a", charge = 1.0f)
                }

                select ("1 oder 2?") either {
                    given("") condition {true}
                    emit event {
                        ChargeCreditCard(reference = "a", charge = 1.0f)
                    }

                } or {
                    given("") condition {false}
                    emit event {
                        ChargeCreditCard(reference = "a", charge = 1.0f)
                    }
                }

                await event CreditCardCharged::class having {"reference" match { "a" };}

                on time duration ("30 seconds") { "PT30S" }

                execute command {
                    PaymentRetrievalAccepted6("1", 25f)
                }

                emit event {
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

