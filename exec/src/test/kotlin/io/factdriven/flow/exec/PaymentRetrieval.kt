package io.factdriven.flow.exec

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

class PaymentRetrieval(init: RetrievePayment) {

    val paymentId = init.id
    val accountId = init.accountId
    var uncovered = init.payment
    var covered = 0F

    fun apply(event: PaymentRetrieved) {
        covered += event.payment ?: uncovered
        uncovered -= event.payment ?: uncovered
    }

}

data class RetrievePayment(val id: String? = null, val accountId: String? = null, val payment: Float)
data class PaymentRetrievalAccepted(val paymentId: String? = null)
data class PaymentRetrieved(val paymentId: String? = null, val payment: Float? = null)
data class PaymentFailed(val paymentId: String? = null)
data class PaymentCoveredManually(val paymentId: String? = null)
data class ChargeCreditCard(val reference: String? = null, val payment: Float? = null)
data class CreditCardCharged(val reference: String? = null)
data class CreditCardExpired(val reference: String? = null)
data class CreditCardDetailsUpdated(val reference: String? = null)
data class WithdrawAmount(val reference: String? = null, val payment: Float? = null)
data class CreditAmount(val reference: String? = null)
data class AmountWithdrawn(val reference: String? = null)
