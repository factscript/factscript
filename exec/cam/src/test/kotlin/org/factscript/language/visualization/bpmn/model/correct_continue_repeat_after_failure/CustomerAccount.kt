package org.factscript.language.visualization.bpmn.model.correct_continue_repeat_after_failure

import org.factscript.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CustomerAccount(fact: WithdrawAmount) {

    val account = fact.account
    val amount = fact.amount

    companion object {

        init {

            flow <CustomerAccount> {

                on command WithdrawAmount::class emit {
                    success event AmountWithdrawn::class
                }

                emit event { AmountWithdrawn(account, amount) }

            }

        }

    }

}

data class WithdrawAmount(val account: String, val amount: Float)
data class AmountWithdrawn(val account: String, val amount: Float)
