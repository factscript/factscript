package io.factdriven.language.visualization.bpmn.model.nesting_gateways

import io.factdriven.language.flow

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

            flow<CreditCardCharge> {

                on command ChargeCreditCard::class promise {
                    report success CreditCardCharged::class
                }

                await event (CreditCardGatewayConfirmationReceived::class) having "reference" match { reference }

                emit event CreditCardCharged::class by {
                    CreditCardCharged(
                        reference,
                        amount
                    )
                }

            }

        }

    }

}

data class ChargeCreditCard(val reference: String, val amount: Float)
data class CreditCardCharged(val reference: String, val amount: Float)
data class CreditCardGatewayConfirmationReceived(val reference: String, val amount: Float)

