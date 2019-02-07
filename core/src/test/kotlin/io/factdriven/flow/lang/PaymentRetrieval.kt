package io.factdriven.flow.lang

import io.factdriven.flow.define

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(command: RetrievePayment) {

    val paymentId: String?
    val accountId: String?
    var uncovered: Float
    var covered: Float

    init {
        this.paymentId = command.id
        this.accountId = command.accountId
        this.uncovered = command.payment
        this.covered = 0F
    }

    fun apply(event: PaymentRetrieved) {

        covered += event.payment ?: uncovered
        uncovered -= event.payment ?: uncovered

    }

    companion object {

        fun init() {}

        init {

            define <PaymentRetrieval> {

                on message(RetrievePayment::class) create this.progress(PaymentRetrievalAccepted::class) by {
                    PaymentRetrievalAccepted(paymentId = it.id)
                }

                execute service {
                    create intention(ChargeCreditCard::class) by { ChargeCreditCard(reference = paymentId, payment = uncovered) }
                    on message(CreditCardCharged::class) having "reference" match { paymentId } create success()
                }

                create success(PaymentRetrieved::class) by {
                    PaymentRetrieved(paymentId = paymentId, payment = uncovered)
                }

            }

        }
    }

}

data class RetrievePayment(val id: String? = null, val accountId: String? = null, val payment: Float)
data class PaymentRetrievalAccepted(val paymentId: String? = null)
data class PaymentCoveredManually(val paymentId: String? = null)
data class PaymentRetrieved(val paymentId: String? = null, val payment: Float? = null)
data class PaymentFailed(val paymentId: String? = null)

data class ChargeCreditCard(val reference: String? = null, val payment: Float? = null)
data class CreditCardCharged(val reference: String? = null)
data class CreditCardExpired(val reference: String? = null)
data class CreditCardDetailsUpdated(val reference: String? = null)

data class WithdrawAmount(val reference: String? = null, val payment: Float? = null)
data class AmountWithdrawn(val reference: String? = null)
data class CreditAmount(val reference: String? = null)
