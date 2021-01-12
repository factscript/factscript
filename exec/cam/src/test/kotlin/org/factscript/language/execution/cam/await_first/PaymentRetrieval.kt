package org.factscript.language.execution.cam.await_first

import org.factscript.language.*

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

                await first {
                    on event CreditCardUnvalidated::class
                    execute command {
                        ChargeCreditCard(
                            reference,
                            1F
                        )
                    }
                } or {
                    on event CreditCardValidated::class
                }

                execute command {
                    ChargeCreditCard(
                        reference,
                        amount - 1F
                    )
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

class CreditCardUnvalidated
class CreditCardValidated
