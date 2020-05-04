package io.factdriven.language.execution.cam.await_time

import io.factdriven.language.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardCharge(fact: ChargeCreditCard) {

    val reference = fact.reference
    val charge = fact.charge

    var open = true; private set
    var failed = false

    fun apply(fact: CreditCardCharged) {
        open = false
    }

    fun apply(fact: CreditCardFailed) {
        open = false
        failed = true
    }

    companion object {

        init {

            flow <CreditCardCharge> {

                on command ChargeCreditCard::class promise {
                    report success CreditCardCharged::class
                    report failure CreditCardFailed::class
                }

                await event ConfirmationReceived::class having "reference" match { reference } but {
                    on time duration ("3 seconds") { "PT3S" }
                    emit event CreditCardFailed::class by { CreditCardFailed(reference)}
                }

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
data class CreditCardFailed(val reference: String, val charge: Float? = null)
