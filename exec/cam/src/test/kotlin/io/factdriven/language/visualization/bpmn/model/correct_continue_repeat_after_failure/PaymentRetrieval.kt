package io.factdriven.language.visualization.bpmn.model.correct_continue_repeat_after_failure

import io.factdriven.language.AwaitEventBut
import io.factdriven.language.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(fact: RetrievePayment) {

    var account = fact.account
    var payment = fact.payment
    var covered = 0F

    companion object {

        init {

            flow <PaymentRetrieval> {

                on command RetrievePayment::class promise {
                    report success PaymentRetrieved::class
                    report failure PaymentFailed::class
                }

                execute command WithdrawAmount::class by {
                    WithdrawAmount(account, payment)
                }

                select ("Payment covered?") either {
                    given ("No") condition { covered < payment }
                    loop {
                        execute command ChargeCreditCard::class by {
                            ChargeCreditCard(account, payment)
                        } but {
                            on event CreditCardExpired::class
                            await event CreditCardDetailsUpdated::class having {
                                "account" match { account }
                            } but {
                                on event PaymentCoveredManually::class having "account" match { account }
                            } but {
                                on time duration ("14 days") { "P14D" }
                                emit event PaymentFailed::class by { PaymentFailed(account, payment) }
                            }
                        }
                        until ("Credit card charged?") condition { covered == payment }
                    }
                } or {
                    given ("Yes")
                }

                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(account, payment)
                }

            }

        }

    }

}

data class RetrievePayment(val account: String, val payment: Float)
data class PaymentRetrieved(val account: String, val payment: Float)
data class PaymentCoveredManually(val account: String, val payment: Float)
data class PaymentFailed(val account: String, val payment: Float)
