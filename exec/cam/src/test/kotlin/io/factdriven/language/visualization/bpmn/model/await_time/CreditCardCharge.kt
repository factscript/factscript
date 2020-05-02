package io.factdriven.language.visualization.bpmn.model.await_time

import io.factdriven.language.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardCharge {

    companion object {

        init {

            flow <CreditCardCharge> {

                on time cycle ("Every month") { "P1M" }

                loop {
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(reference = "1234432112344321", charge = 3F)
                    } but {
                        on time duration ("30 seconds") { "PT30S" }
                        emit event CreditCardFailed::class by { CreditCardFailed() }
                        await time duration ("7 days") { "P7D" }
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
