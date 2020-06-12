package io.factdriven.language.execution.aws.example.function

import io.factdriven.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardCharge(fact: ChargeCreditCard) {

    val reference = fact.reference
    val charge = fact.charge

    companion object {

        init {

            flow <CreditCardCharge> {
                on command ChargeCreditCard::class emit { success event CreditCardCharged::class }

                on time duration ("Wait 1 Minute") { "PT1M" }

                emit success event { CreditCardCharged(reference = reference, charge = charge) }
            }

        }

    }

}

data class ChargeCreditCard(val reference: String, val charge: Float)
data class ConfirmationReceived(val reference: String)
data class CreditCardCharged(val reference: String, val charge: Float? = null)
