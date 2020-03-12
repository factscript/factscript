package io.factdriven.traverse.examples.payment


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

            define <PaymentRetrieval> {

                on command RetrievePayment::class

                select ("Payment (partly) uncovered?") either {
                    given ("Yes") condition { amount == 0f }

                    execute command PaymentRetrieved::class by {
                        PaymentRetrieved(0f)
                    }
                } or {
                    given ("No") // = default path w/o condition

                    execute command PaymentRetrieved::class by {
                        PaymentRetrieved2(0f)
                    }
                }

                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(1f)
                }
            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
data class PaymentRetrieved2(val amount: Float)
