package io.factdriven.flow.camunda

import io.factdriven.language.flow
import kotlin.math.max

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class CustomerAccount(val fact: WithdrawAmount) {

    private val name: String = fact.name
    private var balance: Float = 0F
    private var pending: Float = 0F

    init {
        pending = fact.amount
    }

    fun apply(fact: AmountWithdrawn) {
        pending = 0F
        balance += fact.amount
    }

    companion object {

        fun init() {

            flow<CustomerAccount> {

                on command WithdrawAmount::class promise {
                    report success AmountWithdrawn::class
                }

                emit event AmountWithdrawn::class by {
                    AmountWithdrawn(name, max(pending, 10F))
                }

            }

        }

    }

}

data class WithdrawAmount(val name: String, val amount: Float)
data class AmountWithdrawn(val name: String, val amount: Float)
