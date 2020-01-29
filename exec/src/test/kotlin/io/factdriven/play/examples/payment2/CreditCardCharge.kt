package io.factdriven.play.examples.payment2

import io.factdriven.lang.define

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class CreditCardCharge(val fact: ChargeCreditCard) {

    private val reference: String = fact.reference
    private val amount: Float = fact.amount
    private var successful: Boolean = false

    fun apply(fact: CreditCardCharged) {
        successful = true
    }

    companion object {

        init {

            define <CreditCardCharge> {

                on command ChargeCreditCard::class

                consume event(CreditCardGatewayConfirmationReceived::class) having "reference" match { reference }

                emit event CreditCardCharged::class by {
                    CreditCardCharged(reference, amount)
                }

            }

        }

    }

}

data class ChargeCreditCard(val reference: String, val amount: Float)
data class CreditCardCharged(val reference: String, val amount: Float)
data class CreditCardGatewayConfirmationReceived(val reference: String, val amount: Float)

