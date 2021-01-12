package org.factscript.language.execution.cam

import org.factscript.language.*
import kotlin.math.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Account1(fact: org.factscript.language.execution.cam.WithdrawAmountFromCustomerAccount) {

    val customer: String = fact.customer
    var balance: Float = 5F
    var pending: Float = min(fact.withdraw, balance)

    fun apply(fact: org.factscript.language.execution.cam.AmountWithdrawnFromCustomerAccount) {
        pending = 0F
        balance -= fact.withdrawn
    }

    companion object {

        init {

            flow <org.factscript.language.execution.cam.Account1> {

                on command org.factscript.language.execution.cam.WithdrawAmountFromCustomerAccount::class emit {
                    success event org.factscript.language.execution.cam.AmountWithdrawnFromCustomerAccount::class
                }

                emit success event { org.factscript.language.execution.cam.AmountWithdrawnFromCustomerAccount(customer, pending) }

            }

        }

    }

}

class Account2(fact: org.factscript.language.execution.cam.CreditAmountToCustomerAccount) {

    val customer = fact.customer
    var balance: Float = 0F
    var pending: Float = fact.credit

    fun load(fact: org.factscript.language.execution.cam.AmountCreditedToCustomerAccount) {
        pending = 0F
        balance += fact.credited
    }

    companion object {

        init {

            flow <org.factscript.language.execution.cam.Account2> {


                on command org.factscript.language.execution.cam.CreditAmountToCustomerAccount::class emit {
                    success event org.factscript.language.execution.cam.AmountCreditedToCustomerAccount::class
                }

                emit success event { org.factscript.language.execution.cam.AmountCreditedToCustomerAccount(customer, pending) }

            }

        }

    }

}

data class WithdrawAmountFromCustomerAccount(val customer: String, val withdraw: Float)
data class AmountWithdrawnFromCustomerAccount(val customer: String, val withdrawn: Float)
data class CreditAmountToCustomerAccount(val customer: String, val credit: Float)
data class AmountCreditedToCustomerAccount(val customer: String, val credited: Float)
