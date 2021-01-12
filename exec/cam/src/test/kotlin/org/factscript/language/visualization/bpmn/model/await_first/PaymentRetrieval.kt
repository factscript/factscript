package org.factscript.language.visualization.bpmn.model.await_first

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
                    on event CreditCardUnvalidated::class
                    execute command { ChargeCreditCard(id, 1F) }
                } or {
                    on event CreditCardValidated::class
                }

                execute command { ChargeCreditCard(id, total - 1F) }

                emit success event { PaymentRetrieved(total) }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
class CreditCardUnvalidated
class CreditCardValidated
