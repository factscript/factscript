package io.factdriven.language.definition.raise_failure

import io.factdriven.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class CreditCardCharge(val fact: ChargeCreditCard) {

    private val reference: String = fact.reference
    private val amount: Float = fact.amount
    private var successful: Boolean = false

    fun apply(fact: CreditCardGatewayConfirmationReceived) {
        successful = !fact.expired
    }

    companion object {

        init {

            flow <CreditCardCharge> {

                on command ChargeCreditCard::class emit {
                    success event CreditCardCharged::class
                    failure event CreditCardExpired::class
                }

                await event (CreditCardGatewayConfirmationReceived::class) having "reference" match { reference }

                select ("Credit card expired?") either {
                    given ("No")
                } or {
                    given ("Yes") condition { !successful }
                    emit failure event { CreditCardExpired(reference) }
                }

                emit success event { CreditCardCharged(reference, amount) }

            }

        }

    }

}

data class ChargeCreditCard(val reference: String, val amount: Float)
data class CreditCardCharged(val reference: String, val amount: Float)
data class CreditCardExpired(val reference: String)
data class CreditCardGatewayConfirmationReceived(val reference: String, val amount: Float, val expired: Boolean)
