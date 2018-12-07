package io.factdriven.flow.lang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

class PaymentRetrieval(init: RetrievePayment) {

    val paymentId = init.id!!
    val accountId = init.accountId!!
    var uncovered = init.payment!!
    var covered = 0F

    fun apply(message: PaymentRetrieved) {
        covered = uncovered
        uncovered = 0F
    }

}

class RetrievePayment(val id: String? = null, val accountId: String? = null, val payment: Float? = null)
class PaymentRetrievalAccepted(val paymentId: String? = null)
class PaymentRetrieved(val paymentId: String? = null)
class PaymentFailed(val paymentId: String? = null)
class PaymentCoveredManually(val paymentId: String? = null)
class ChargeCreditCard(val reference: String? = null, val payment: Float? = null)
class CreditCardCharged(val reference: String? = null)
class CreditCardExpired(val reference: String? = null)
class CreditCardDetailsUpdated(val reference: String? = null)
class WithdrawAmount(val reference: String? = null, val payment: Float? = null)
class CreditAmount(val reference: String? = null)
class AmountWithdrawn(val reference: String? = null)
