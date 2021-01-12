package org.factscript.language.execution.cam.compensation

import org.factscript.language.*
import kotlin.math.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Account1(fact: WithdrawAmountFromCustomerAccount) {

    val customer: String = fact.customer
    var balance: Float = 5F
    var pending: Float = min(fact.withdraw, balance)

    fun apply(fact: AmountWithdrawnFromCustomerAccount) {
        pending = 0F
        balance -= fact.withdrawn
    }

    companion object {

        init {

            flow <Account1> {

                on command WithdrawAmountFromCustomerAccount::class emit {
                    success event AmountWithdrawnFromCustomerAccount::class
                }

                emit success event { AmountWithdrawnFromCustomerAccount(customer, pending) }

            }

        }

    }

}

class Account2(fact: CreditAmountToCustomerAccount) {

    val customer = fact.customer
    var balance: Float = 0F
    var pending: Float = fact.credit

    fun load(fact: AmountCreditedToCustomerAccount) {
        pending = 0F
        balance += fact.credited
    }

    companion object {

        init {

            flow <Account2> {

                on command CreditAmountToCustomerAccount::class emit {
                    success event AmountCreditedToCustomerAccount::class
                }

                emit success event { AmountCreditedToCustomerAccount(customer, pending) }

            }

        }

    }

}

data class WithdrawAmountFromCustomerAccount(val customer: String, val withdraw: Float)
data class AmountWithdrawnFromCustomerAccount(val customer: String, val withdrawn: Float)
data class CreditAmountToCustomerAccount(val customer: String, val credit: Float)
data class AmountCreditedToCustomerAccount(val customer: String, val credited: Float)
