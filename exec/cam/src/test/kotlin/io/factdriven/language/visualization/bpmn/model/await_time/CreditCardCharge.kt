package io.factdriven.language.visualization.bpmn.model.await_time

import io.factdriven.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardCharge {

    companion object {

        init {

            flow <CreditCardCharge> {

                on command ChargeCreditCard::class emit {
                    success event CreditCardCharged::class
                    failure event CreditCardFailed::class
                }

                execute command {
                    ChargeCreditCard(reference = "1234432112344321", charge = 3F)
                } but {
                    on time duration ("30 seconds") { "PT30S" }
                    emit event CreditCardFailed::class
                }

                emit event {
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
