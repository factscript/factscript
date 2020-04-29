package io.factdriven.language.definition.start_and_end_event

import io.factdriven.language.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(fact: RetrievePayment) {

    val amount = fact.amount

    companion object {

        init {
            flow<PaymentRetrieval> {
                on command RetrievePayment::class
                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(amount)
                }
            }
        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
