package io.factdriven.language.visualization.bpmn.model.await_time

import io.factdriven.language.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardCharge {

    companion object {

        init {

            flow <CreditCardCharge> {

                on command ChargeCreditCard::class promise {
                    report success CreditCardCharged::class
                    report failure CreditCardFailed::class
                }

                loop {
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(reference = "1234432112344321", charge = 3F)
                    } but {
                        on event CreditCardFailed::class
                        await event CreditCardDetailsUpdated::class but {
                            on time duration ("Two weeks") { "P14D" }
                            emit event CreditCardFailed::class
                        }
                    }
                    until ("Credit card succeeded") condition { true }
                }

                emit event CreditCardCharged::class by {
                    CreditCardCharged(reference = "abc", charge = 3F)
                }

            }

        }

    }

}

data class ChargeCreditCard(val reference: String, val charge: Float)
data class CreditCardCharged(val reference: String, val charge: Float? = null)
class CreditCardFailed()
class CreditCardDetailsUpdated()
