package org.factscript.language.visualization.bpmn.model.execute_all

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

                on command RetrievePayment::class emit {
                    success event PaymentRetrieved::class
                }

                execute all {
                    execute command { ChargeCreditCard(id, total - covered) }
                } and {
                    execute command { ChargeCreditCard(id, total - covered) }
                    execute command { ChargeCreditCard(id, total - covered) }
                } and {
                    execute command { ChargeCreditCard(id, total - covered) }
                    emit event { PaymentRetrieved(total) }
                }

                emit event { PaymentRetrieved(total) }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
