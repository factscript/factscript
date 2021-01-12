package org.factscript.language.execution.cam.compensation

import org.factscript.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class CreditCard (

    val reference: String,
    val charge: Float,
    var confirmed: Boolean = false,
    var successful: Boolean = false

){

    constructor(fact: ChargeCreditCard): this(fact.reference, fact.charge)

    fun apply(fact: ConfirmationReceived) {
        confirmed = fact.valid
    }

    fun apply(fact: CreditCardCharged) {
        successful = true
    }

    companion object {

        init {

            flow <CreditCard> {

                on command ChargeCreditCard::class emit {
                    success event CreditCardCharged::class
                    failure event CreditCardExpired::class
                }

                await event ConfirmationReceived::class having "reference" match { reference }

                select either {
                    given ("Yes") condition { confirmed }
                } or {
                    otherwise ("No")
                    emit failure event { CreditCardExpired(reference, charge)}
                }

                emit success event { CreditCardCharged(reference, charge) }

            }

        }

    }
}

data class ChargeCreditCard(val reference: String, val charge: Float)
data class CreditCardExpired(val reference: String, val charge: Float)
data class ConfirmationReceived(val reference: String, val valid: Boolean)
data class CreditCardCharged(val reference: String, val charge: Float)
