package org.factscript.language.definition.await_first

import org.factscript.language.*
import java.util.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(fact: RetrievePayment) {

    var id = UUID.randomUUID().toString()
    var total = fact.amount
    var covered = 0F

    companion object {

        init {

            flow<PaymentRetrieval> {

                on command RetrievePayment::class

                await first {
                    on event PaymentRetrieved::class
                    execute command { ChargeCreditCard(id, total - covered) }
                    execute command { ChargeCreditCard(id, total - covered) }
                } or {
                    on event PaymentFailed::class
                    execute command { ChargeCreditCard(id, total - covered) }
                }

                emit event { PaymentRetrieved(total) }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
data class PaymentFailed(val amount: Float)
