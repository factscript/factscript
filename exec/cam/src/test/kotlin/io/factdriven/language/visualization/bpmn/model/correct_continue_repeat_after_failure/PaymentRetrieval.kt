package io.factdriven.language.visualization.bpmn.model.correct_continue_repeat_after_failure

import io.factdriven.language.*

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

                on command RetrievePayment::class emit {
                    success event PaymentRetrieved::class
                    failure event PaymentFailed::class
                }

                execute command {
                    WithdrawAmount(account, payment)
                }

                select ("Payment completely covered?") either {

                    given ("No") condition { covered != payment }

                    execute loop {

                        execute command {
                            ChargeCreditCard(account, payment)
                        } but {
                            on event CreditCardExpired::class
                            await first {
                                on event CreditCardDetailsUpdated::class having "account" match { account }
                            } or {
                                on time duration ("14 days") { "PT3M" }
                                emit event { PaymentFailed(account, payment) }
                            }
                        }

                        until ("Payment completely covered?") condition { covered == payment }

                    }

                } or {

                    given ("Yes")

                    emit event { PaymentRetrieved(account, payment) }

                }

                emit event { PaymentRetrieved(account, payment) }

            }

        }

    }

}

data class RetrievePayment(val account: String, val payment: Float)
data class PaymentRetrieved(val account: String, val payment: Float)
data class PaymentFailed(val account: String, val payment: Float)
