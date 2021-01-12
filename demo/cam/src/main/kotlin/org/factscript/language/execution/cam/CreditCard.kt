package org.factscript.language.execution.cam

import org.factscript.language.*
import java.util.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class CreditCard (

    val reference: String,
    val charge: Float,
    var confirmed: Boolean = false,
    var successful: Boolean = false

){

    constructor(fact: org.factscript.language.execution.cam.ChargeCreditCard): this(fact.reference, fact.charge)

    fun apply(fact: org.factscript.language.execution.cam.CreditCardProcessed) {
        confirmed = fact.valid
    }

    fun apply(fact: org.factscript.language.execution.cam.CreditCardCharged) {
        successful = true
    }

    companion object {

        init {

            flow <org.factscript.language.execution.cam.CreditCard> {

                on command org.factscript.language.execution.cam.ChargeCreditCard::class emit {
                    success event org.factscript.language.execution.cam.CreditCardCharged::class
                    failure event org.factscript.language.execution.cam.CreditCardExpired::class
                }

                await event org.factscript.language.execution.cam.CreditCardProcessed::class having "reference" match { reference }

                select either {
                    given ("Yes") condition { confirmed }
                } or {
                    otherwise ("No")
                    emit failure event { org.factscript.language.execution.cam.CreditCardExpired(reference, charge) }
                }

                emit success event { org.factscript.language.execution.cam.CreditCardCharged(reference, charge) }

            }

        }

    }
}

data class ChargeCreditCard(val reference: String, val charge: Float)
data class CreditCardExpired(val reference: String, val charge: Float)
data class CreditCardProcessed(val reference: String, val valid: Boolean)
data class CreditCardCharged(val reference: String, val charge: Float)
