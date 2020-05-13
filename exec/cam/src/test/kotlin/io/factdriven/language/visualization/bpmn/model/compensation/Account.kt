package io.factdriven.language.visualization.bpmn.model.compensation

import io.factdriven.language.*
import kotlin.math.max

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Account(fact: WithdrawAmountFromCustomerAccount) {

    val customer: String = fact.customer
    var balance: Float = 0F
    var pending: Float = fact.withdraw

    fun apply(fact: AmountWithdrawnFromCustomerAccount) {
        pending = 0F
        balance -= fact.withdrawn
    }

    fun apply(fact: CreditAmountToCustomerAccount) {
        pending = fact.credit
    }

    fun apply(fact: AmountCreditedToCustomerAccount) {
        pending = 0F
        balance += fact.credited
    }

    companion object {

        init {

            flow <Account> {

                on command WithdrawAmountFromCustomerAccount::class emit {
                    success event AmountWithdrawnFromCustomerAccount::class
                }

                emit success event { AmountWithdrawnFromCustomerAccount(customer, pending) }

                on command CreditAmountToCustomerAccount::class having "customer" match { customer } emit {
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
