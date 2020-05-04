package io.factdriven.language.execution.cam.handle_failure

import io.factdriven.language.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class CreditCardCharge(val fact: ChargeCreditCard) {

    private val reference: String = fact.reference
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
                    report failure PaymentAmountTooHigh::class
                }

                await event (CreditCardGatewayConfirmationReceived::class) having "reference" match { reference }

                select ("Credit card expired?") either {
                    given ("No")
                } or {
                    given ("Yes") condition { !successful }
                    emit event CreditCardExpired::class by { CreditCardExpired(reference) }
                } or {
                    given ("Amount too high") condition { amount >= 1000 }
                    emit event PaymentAmountTooHigh::class by { PaymentAmountTooHigh(reference) }
                }

                emit event CreditCardCharged::class by { CreditCardCharged(reference, amount) }

            }

        }

    }

}

data class ChargeCreditCard(val reference: String, val amount: Float)
data class CreditCardCharged(val reference: String, val amount: Float)
data class CreditCardExpired(val reference: String)
data class PaymentAmountTooHigh(val reference: String)
data class CreditCardGatewayConfirmationReceived(val reference: String, val amount: Float)
