package org.factscript.language.execution.cam

import org.factscript.language.*
import java.time.LocalDateTime

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Payment (

    val orderId: String,
    val accountId: String,
    var total: Float,
    var covered: Float = 0F,
    var expired: Boolean = false,
    var successful: Boolean = false,
    var failed: Boolean = false

) {

    constructor(fact: org.factscript.language.execution.cam.RetrievePayment): this(fact.orderId, fact.accountId, fact.payment)

    fun apply(fact: org.factscript.language.execution.cam.AmountWithdrawnFromCustomerAccount) {
        covered += fact.withdrawn
    }

    fun apply(fact: org.factscript.language.execution.cam.AmountCreditedToCustomerAccount) {
        covered -= fact.credited
    }

    fun apply(fact: org.factscript.language.execution.cam.CreditCardCharged) {
        covered += fact.charge
    }

    fun apply(fact: org.factscript.language.execution.cam.CreditCardExpired) {
        expired = true
    }

    fun apply(fact: org.factscript.language.execution.cam.PaymentRetrieved) {
        successful = true
    }

    fun apply(fact: org.factscript.language.execution.cam.PaymentFailed) {
        failed = true
    }

    companion object {

        init {

            flow <org.factscript.language.execution.cam.Payment> {

                on command org.factscript.language.execution.cam.RetrievePayment::class emit {
                    success event org.factscript.language.execution.cam.PaymentRetrieved::class
                    failure event org.factscript.language.execution.cam.PaymentFailed::class
                }

                execute command {
                    org.factscript.language.execution.cam.WithdrawAmountFromCustomerAccount(customer = accountId, withdraw = total)
                } but {
                    on failure org.factscript.language.execution.cam.PaymentFailed::class
                    execute command {
                        org.factscript.language.execution.cam.CreditAmountToCustomerAccount(customer = accountId, credit = covered)
                    }
                }

                select ("Payment fully covered?") either {
                    given ("No") condition { covered < total }
                    repeat {
                        execute command {
                            org.factscript.language.execution.cam.ChargeCreditCard(orderId, total - covered)
                        } but {
                            on failure org.factscript.language.execution.cam.CreditCardExpired::class
                            await first {
                                on event org.factscript.language.execution.cam.CreditCardDetailsUpdated::class having "accountId" match { accountId }
                            } or {
                                on time duration ("Two weeks") { "PT5M" }
                                emit failure event { org.factscript.language.execution.cam.PaymentFailed(orderId) }
                            }
                        }
                        until ("Payment fully covered?") condition { covered == total }
                    }
                } or {
                    otherwise ("Yes")
                    emit success event { org.factscript.language.execution.cam.PaymentRetrieved(orderId) }
                }

                emit success event { org.factscript.language.execution.cam.PaymentRetrieved(orderId) }
            }

        }

    }

}

data class RetrievePayment(val orderId: String, val accountId: String, val payment: Float)
data class PaymentRetrieved(val orderId: String)
data class PaymentFailed(val orderId: String)
data class CreditCardDetailsUpdated(val accountId: String)
