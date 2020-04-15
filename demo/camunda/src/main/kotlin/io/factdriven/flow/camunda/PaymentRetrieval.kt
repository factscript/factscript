package io.factdriven.flow.camunda

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

    var pending = 0F

    fun apply(fact: ChargeCreditCard) {

        pending = fact.charge

    }

    fun apply(fact: CreditCardCharged) {

        covered += fact.charge ?: pending

    }

    companion object {

        fun init() {

            flow <PaymentRetrieval> {

                on command RetrievePayment::class promise {
                    report success PaymentRetrieved::class
                }

                execute command ChargeCreditCard::class by {
                    ChargeCreditCard(reference = paymentId, charge = 1F)
                }

                execute command ChargeCreditCard::class by {
                    ChargeCreditCard(reference = paymentId, charge = total - covered)
                }

                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(paymentId = paymentId, payment = total)
                }

            }

        }

    }

}

data class RetrievePayment(val reference: String, val accountId: String, val payment: Float)
data class PaymentRetrieved(val paymentId: String, val payment: Float)

