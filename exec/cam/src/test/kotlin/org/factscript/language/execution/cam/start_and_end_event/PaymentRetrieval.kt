package org.factscript.language.execution.cam.start_and_end_event

import org.factscript.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(fact: RetrievePayment) {

    val amount = fact.amount
    var retrieved = false; private set

    fun apply(fact: PaymentRetrieved) {
        retrieved = true
    }

    companion object {

        init {
            flow<PaymentRetrieval> {
                on command RetrievePayment::class
                emit event {
                    PaymentRetrieved(amount)
                }
            }
        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
