package io.factdriven.language.visualization.bpmn.model.correct_continue_repeat_after_failure

import io.factdriven.language.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class CreditCardCharge(val fact: ChargeCreditCard) {

    private val reference: String = fact.account
    private val amount: Float = fact.amount
    private var successful: Boolean = false

    fun apply(fact: CreditCardGatewayConfirmationReceived) {
        successful = fact.amount > 0
    }

    companion object {

        init {

            flow<CreditCardCharge> {

                on command ChargeCreditCard::class promise {
                    report success CreditCardCharged::class
                    report failure CreditCardExpired::class
                }

                await event (CreditCardGatewayConfirmationReceived::class) having "reference" match { reference }

                select ("Credit card expired?") either {
                    given ("No")
                } or {
                    given ("Yes") condition { !successful }
                    emit event CreditCardExpired::class by { CreditCardExpired(reference) }
                }

                emit event CreditCardCharged::class by { CreditCardCharged(reference, amount) }

            }

        }

    }

}

data class ChargeCreditCard(val account: String, val amount: Float)
data class CreditCardCharged(val account: String, val amount: Float)
data class CreditCardDetailsUpdated(val account: String, val amount: Float)
data class CreditCardExpired(val account: String)
data class CreditCardGatewayConfirmationReceived(val account: String, val amount: Float)

