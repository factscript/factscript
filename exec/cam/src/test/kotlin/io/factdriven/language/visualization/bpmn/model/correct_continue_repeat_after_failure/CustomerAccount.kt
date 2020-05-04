package io.factdriven.language.visualization.bpmn.model.correct_continue_repeat_after_failure

import io.factdriven.language.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CustomerAccount(fact: WithdrawAmount) {

    val account = fact.account
    val amount = fact.amount

    companion object {

        init {

            flow <CustomerAccount> {

                on command WithdrawAmount::class promise {
                    report success AmountWithdrawn::class
                }

                emit event AmountWithdrawn::class by { AmountWithdrawn(account, amount) }

            }

        }

    }

}

data class WithdrawAmount(val account: String, val amount: Float)
data class AmountWithdrawn(val account: String, val amount: Float)
