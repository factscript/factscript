package io.factdriven.language.execution.cam

import io.factdriven.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Payment(

    val orderId: String,
    val accountId: String,
    var total: Float,
    var covered: Float = 0F

) {

    constructor(fact: RetrievePayment): this(fact.orderId, fact.accountId, fact.payment)

    fun apply(fact: AmountWithdrawnFromCustomerAccount) {
        covered += fact.withdrawn
    }

    fun apply(fact: CreditCardCharged) {
        covered += fact.charge
    }

    companion object {

        init {

            flow <Payment> {

                on command RetrievePayment::class emit {
                    success event PaymentRetrieved::class
                    failure event PaymentFailed::class
                }

                execute command {
                    WithdrawAmountFromCustomerAccount(name = accountId, withdraw = total)
                } but {
                    on failure PaymentFailed::class
                    execute command {
                        CreditAmountToCustomerAccount(name = accountId, credit = covered)
                    }
                }

                select("Payment fully covered?") either {

                    given ("No") condition { covered == total }

                    execute loop {
                        execute command {
                            ChargeCreditCard(orderId,total - covered)
                        } but {
                            on failure CreditCardExpired::class
                            await first {
                                on event CreditCardDetailsUpdated::class having "accountId" match { accountId }
                            } or {
                                on time duration ("Two weeks") { "P14D" }
                                emit failure event { PaymentFailed(orderId) }
                            }
                        }
                        until ("Payment fully covered?") condition { covered == total }
                    }

                } or {

                    otherwise ("Yes")
                    emit success event { PaymentRetrieved(orderId) }

                }

                emit success event { PaymentRetrieved(orderId) }

            }

        }

    }

}

data class RetrievePayment(val orderId: String, val accountId: String, val payment: Float)
data class PaymentRetrieved(val orderId: String)
data class PaymentFailed(val orderId: String)
