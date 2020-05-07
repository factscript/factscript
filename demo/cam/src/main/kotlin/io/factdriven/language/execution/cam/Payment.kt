package io.factdriven.language.execution.cam

import io.factdriven.language.flow

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
        covered += fact.maximum
    }

    fun apply(fact: CreditCardCharged) {
        covered += fact.charge
    }

    companion object {

        init {

            flow <Payment> {

                on command RetrievePayment::class promise {
                    report success PaymentRetrieved::class
                    report failure PaymentFailed::class
                }

                execute command WithdrawAmountFromCustomerAccount::class by {
                    WithdrawAmountFromCustomerAccount(
                        name = accountId,
                        maximum = total
                    )
                }

                    select("Payment fully covered?") either {
                        given ("No") condition { covered == total }
                        loop {
                            execute command ChargeCreditCard::class by {
                                ChargeCreditCard(
                                    orderId,
                                    total - covered
                                )
                            } but {
                                on event CreditCardExpired::class
                                await first {
                                    on event CreditCardDetailsUpdated::class having "accountId" match { accountId }
                                } or {
                                    on time duration ("Two weeks") { "P14D" }
                                    emit event PaymentFailed::class by {
                                        PaymentFailed(
                                            orderId
                                        )
                                    }
                                }
                            }
                            until ("Payment fully covered?") condition { covered == total }
                        }
                    } or {
                        given ("Yes")
                        emit event PaymentRetrieved::class by {
                            PaymentRetrieved(
                                orderId
                            )
                        }
                    }

                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(
                        orderId
                    )
                }

            }

        }

    }

}

data class RetrievePayment(val orderId: String, val accountId: String, val payment: Float)
data class PaymentRetrieved(val orderId: String)
data class PaymentFailed(val orderId: String)
