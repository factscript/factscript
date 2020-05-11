package io.factdriven.language.execution.cam.execute_all

import io.factdriven.language.*
import kotlin.math.max
import kotlin.math.min

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(fact: RetrievePayment) {

    val reference = "SomeReference"
    val amount = fact.amount
    var charged = 0F
    var retrieved = false; private set

    fun apply(fact: PaymentRetrieved) {
        retrieved = true
    }

    fun apply(fact: CreditCardCharged) {
        charged += fact.amount
    }

    companion object {

        init {

            flow<PaymentRetrieval> {

                on command RetrievePayment::class

                execute all {
                    execute command {
                        ChargeCreditCard(
                            reference,
                            min(amount, 10F)
                        )
                    }
                } and {
                    execute command {
                        ChargeCreditCard(
                            reference,
                            max(amount - 10, 0F)
                        )
                    }
                }

                emit event {
                    PaymentRetrieved(amount)
                }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
