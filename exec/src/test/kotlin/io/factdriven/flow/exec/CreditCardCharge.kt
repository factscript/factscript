package io.factdriven.flow.exec

import io.factdriven.flow.define

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class CreditCardCharge (

    val reference: String,
    val charge: Float

){

    constructor(command: ChargeCreditCard): this(command.reference, command.charge)

    companion object {

        fun init() {

            define <CreditCardCharge> {

                on message(ChargeCreditCard::class)

                create success(CreditCardCharged::class) by {
                    CreditCardCharged(reference = reference, charge = charge)
                }

            }

        }

    }
}


data class ChargeCreditCard(val reference: String, val charge: Float)
data class CreditCardCharged(val reference: String, val charge: Float? = null)
