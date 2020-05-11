package io.factdriven.language.execution.cam

import io.factdriven.language.*
import kotlin.math.max

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Account(val fact: WithdrawAmountFromCustomerAccount) {

    private val name: String = fact.name
    private var balance: Float = 0F
    private var pending: Float = 0F

    init {
        pending = fact.withdraw
    }

    fun apply(fact: AmountWithdrawnFromCustomerAccount) {
        pending = 0F
        balance += fact.withdrawn
    }

    companion object {

        init {

            flow <Account> {

                on command WithdrawAmountFromCustomerAccount::class emit {
                    success event AmountWithdrawnFromCustomerAccount::class
                }

                emit success event { AmountWithdrawnFromCustomerAccount(name, max(pending, 10F)) }

            }

            flow <Account> {

                on command CreditAmountToCustomerAccount::class emit {
                    success event AmountCreditedToCustomerAccount::class
                }

                emit success event { AmountCreditedToCustomerAccount(name, max(pending, 10F)) }

            }

        }

    }

}

data class WithdrawAmountFromCustomerAccount(val name: String, val withdraw: Float)
data class AmountWithdrawnFromCustomerAccount(val name: String, val withdrawn: Float)
data class CreditAmountToCustomerAccount(val name: String, val credit: Float)
data class AmountCreditedToCustomerAccount(val name: String, val maximum: Float)
