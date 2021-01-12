package org.factscript.language.execution.cam.select_all

import org.factscript.language.*
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

                select all {
                    given(">0") condition { amount > 0 }
                    execute command {
                        ChargeCreditCard(
                            reference,
                            min(amount, 10F)
                        )
                    }
                } or {
                    given(">10") condition { amount > 10 }
                    execute command {
                        ChargeCreditCard(
                            reference,
                            amount - 10
                        )
                    }
                } or {
                    given("0")
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
