package io.factdriven.language.execution.cam.await_event

import io.factdriven.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardCharge(fact: ChargeCreditCard) {

    val reference = fact.reference
    val charge = fact.charge

    var open = true; private set
    val closed: Boolean get() = !open

    fun apply(fact: CreditCardCharged) {
        open = false
    }

    companion object {

        init {

            flow <CreditCardCharge> {

                on command ChargeCreditCard::class

                await event ConfirmationReceived::class having {
                    "reference" match { reference }
                }

                emit event {
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
