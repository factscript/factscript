package io.factdriven.execution.examples.payment2

import io.factdriven.language.define

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(fact: RetrievePayment) {

    val reference = "SomeReference"
    val amount = fact.amount
    var retrieved = false; private set

    fun apply(fact: PaymentRetrieved) {
        retrieved = true
    }

    companion object {

        init {

            define<PaymentRetrieval> {

                on command RetrievePayment::class

                issue command ChargeCreditCard::class by {
                    ChargeCreditCard(reference, amount)
                }

                consume event CreditCardCharged::class having "reference" match { reference }

                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(amount)
                }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
