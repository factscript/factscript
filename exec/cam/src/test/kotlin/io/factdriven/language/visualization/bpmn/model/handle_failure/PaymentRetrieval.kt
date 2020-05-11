package io.factdriven.language.visualization.bpmn.model.handle_failure

import io.factdriven.language.*
import java.time.*
import java.time.LocalDateTime.now
import java.util.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(fact: RetrievePayment) {

    var id = UUID.randomUUID().toString()
    var total = fact.amount

    companion object {

        init {

            flow <PaymentRetrieval> {

                on command RetrievePayment::class promise {
                    report success PaymentRetrieved::class
                    report failure PaymentFailed::class
                }

                execute command {
                    ChargeCreditCard(id, total)
                } but {
                    on failure CreditCardExpired::class
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
