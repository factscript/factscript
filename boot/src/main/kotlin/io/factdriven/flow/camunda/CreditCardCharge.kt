package io.factdriven.flow.camunda

import io.factdriven.flow.define

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class CreditCardCharge (

    val reference: String,
    val charge: Float,
    var successful: Boolean = false

){

    constructor(command: ChargeCreditCard): this(command.reference, command.charge)

    fun apply(event: CreditCardCharged) {
        successful = true
    }

    companion object {

        fun init() {

            define <CreditCardCharge> {

                on message(ChargeCreditCard::class) create progress()

                on message(ConfirmationReceived::class) having "reference" match { reference } create progress()

                create success(CreditCardCharged::class) by {
                    CreditCardCharged(reference = reference, charge = charge)
                }

            }

        }

    }
}


data class ChargeCreditCard(val reference: String, val charge: Float)
data class ConfirmationReceived(val reference: String)
data class CreditCardCharged(val reference: String, val charge: Float? = null)
