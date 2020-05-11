package io.factdriven.language.visualization.bpmn.model.await_event

import io.factdriven.language.*

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
                await event ConfirmationReceived::class having "reference" match { reference }
                emit event { CreditCardCharged(reference, charge) }
            }

        }

    }

}

data class ChargeCreditCard(val reference: String, val charge: Float)
data class ConfirmationReceived(val reference: String)
data class CreditCardCharged(val reference: String, val charge: Float? = null)
