package io.factdriven.execution.camunda.model.creditcard1

import io.factdriven.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardCharge(fact: ChargeCreditCard) {

    val reference = fact.reference
    val charge = fact.charge

    companion object {

        init {
            flow<CreditCardCharge> {
                on command ChargeCreditCard::class
                consume event ConfirmationReceived::class having "reference" match { reference }
                emit event CreditCardCharged::class by {
                    CreditCardCharged(
                        reference = reference,
                        charge = charge
                    )
                }
            }
        }

    }

}

data class ChargeCreditCard(val reference: String, val charge: Float)
data class ConfirmationReceived(val reference: String)
data class CreditCardCharged(val reference: String, val charge: Float? = null)
