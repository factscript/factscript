package io.factdriven.language.visualization.bpmn.model.issue_command

import io.factdriven.language.*
import java.util.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(fact: RetrievePayment) {

    var id = UUID.randomUUID().toString()
    var total = fact.amount

    companion object {

        init {

            flow<PaymentRetrieval> {

                on command RetrievePayment::class

                issue command {
                    ChargeCreditCard(id, total)
                }

                await event CreditCardCharged::class having "id" match { id }

                emit event {
                    PaymentRetrieved(total)
                }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
