package io.factdriven.language.execution.cam

import io.factdriven.language.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class CreditCard (

    val reference: String,
    val charge: Float,
    var successful: Boolean = false

){

    constructor(fact: ChargeCreditCard): this(fact.reference, fact.charge)

    fun apply(fact: CreditCardCharged) {
        successful = true
    }

    companion object {

        init {

            flow <CreditCard> {

                on command ChargeCreditCard::class promise {
                    report success CreditCardCharged::class
                }

                await event ConfirmationReceived::class having "reference" match { reference }

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
data class CreditCardExpired(val reference: String, val charge: Float)
data class CreditCardDetailsUpdated(val reference: String, val charge: Float)
data class ConfirmationReceived(val reference: String)
data class CreditCardCharged(val reference: String, val charge: Float)
