package io.factdriven.flow.camunda

import io.factdriven.lang.define

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class CreditCardCharge (

    val reference: String,
    val charge: Float,
    var successful: Boolean = false

){

    constructor(fact: ChargeCreditCard): this(fact.reference, fact.charge)

    fun apply(fact: CreditCardCharged) {
        successful = true
    }

    companion object {

        fun init() {

            define <CreditCardCharge> {

                on command ChargeCreditCard::class promise {
                    report success CreditCardCharged::class
                }

                consume event ConfirmationReceived::class having "reference" match { reference }

                emit  event CreditCardCharged::class by {
                    CreditCardCharged(reference = reference, charge = charge)
                }

            }

        }

    }
}


data class ChargeCreditCard(val reference: String, val charge: Float)
data class ConfirmationReceived(val reference: String)
data class CreditCardCharged(val reference: String, val charge: Float? = null)
