package org.factscript.language.execution.cam.handle_failure

import org.factscript.language.*
import org.factscript.language.impl.utils.Id

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(fact: RetrievePayment) {

    var id = Id(fact)
    var total = fact.amount
    var covered = 0F
    var successful = false
    var ended = false
    var toohigh = false

    fun apply(fact: PaymentRetrieved) {
        successful = true
        covered = total
        ended = true
    }

    fun apply(fact: PaymentAmountTooHigh) {
        toohigh = true
    }

    fun apply(fact: PaymentFailed) {
        ended = true
    }

    companion object {

        init {

            flow <PaymentRetrieval> {

                on command RetrievePayment::class emit {
                    success event PaymentRetrieved::class
                    failure event PaymentFailed::class
                }

                execute command {
                    ChargeCreditCard(id, total)
                } but {
                    on event CreditCardExpired::class
                    emit event { PaymentFailed() }
                } but {
                    on event PaymentAmountTooHigh::class
                    emit event { PaymentFailed() }
                }

                emit event {
                    PaymentRetrieved(total)
                }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
class PaymentFailed
