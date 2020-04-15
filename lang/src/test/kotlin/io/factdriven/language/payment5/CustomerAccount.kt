package io.factdriven.language.payment5

import io.factdriven.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class CustomerAccount(val fact: CreateAccount) {

    private val name: String = fact.name
    private var balance: Float = 0F
    private var pending: Float = 0F

    fun apply(fact: WithdrawAmount) {
        pending = fact.amount
    }

    fun apply(fact: AmountWithdrawn) {
        pending = 0F
        balance += fact.amount
    }

    companion object {

        init {

            flow<CustomerAccount> {

                on command WithdrawAmount::class promise {
                    report success AmountWithdrawn::class
                }

                emit event AmountWithdrawn::class by {
                    AmountWithdrawn(name, pending)
                }

            }

        }

    }

}

data class CreateAccount(val name: String)
data class WithdrawAmount(val name: String, val amount: Float)
data class AmountWithdrawn(val name: String, val amount: Float)
