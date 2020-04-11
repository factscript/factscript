package io.factdriven.impl.execution.examples.payment4

import io.factdriven.flow

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

            flow<PaymentRetrieval> {

                on command RetrievePayment::class

                select("Payment (partly) uncovered?") either {
                    given("Yes") condition { amount > 0 }
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(reference, amount)
                    }
                } or {
                    given("No") // = default path w/o condition
                }

                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(amount)
                }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
