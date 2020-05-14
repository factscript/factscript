package io.factdriven.language.visualization.bpmn.model.compensation

import io.factdriven.language.*
import kotlin.math.max

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

data class WithdrawAmountFromCustomerAccount(val customer: String, val withdraw: Float)
data class AmountWithdrawnFromCustomerAccount(val customer: String, val withdrawn: Float)
data class CreditAmountToCustomerAccount(val customer: String, val credit: Float)
data class AmountCreditedToCustomerAccount(val customer: String, val credited: Float)
