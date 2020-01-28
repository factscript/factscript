package io.factdriven.play.examples.payment2

import io.factdriven.lang.define
import java.util.*

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

                notice event CreditCardCharged::class having "reference" match { reference }

                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(amount)
                }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
