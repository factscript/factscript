package io.factdriven.flow.camunda

import io.factdriven.language.flow

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

    fun apply(fact: AmountWithdrawn) {
        covered += fact.amount
    }

    fun apply(fact: CreditCardCharged) {
        covered += fact.charge
    }

    companion object {

        fun init() {

            flow<PaymentRetrieval> {

                on command RetrievePayment::class promise {
                    report success PaymentRetrieved::class
                }

                execute command WithdrawAmount::class by {
                    WithdrawAmount(name = accountId, amount = total)
                }

                select("Payment (partly) uncovered?") either {
                    given("Yes") condition { covered < total }
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(reference = paymentId, charge = total - covered)
                    }
                } or {
                    given("No")
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

