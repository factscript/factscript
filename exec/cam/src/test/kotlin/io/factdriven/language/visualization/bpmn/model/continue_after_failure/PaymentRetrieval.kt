package io.factdriven.language.visualization.bpmn.model.continue_after_failure

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

            flow <PaymentRetrieval> {

                on command RetrievePayment::class emit {
                    success event PaymentRetrieved::class
                    failure event PaymentFailed::class
                }

                execute command {
                    ChargeCreditCard(id, total)
                } but {
                    on event CreditCardGatewayConfirmationReceived::class
                } but {
                    on event CreditCardExpired::class
                    execute command { ChargeCreditCard(id, total) }
                    emit event { PaymentFailed() }
                } but {
                    on time duration ("Two weeks") { "PT5S" }
                    execute command { ChargeCreditCard(id, total) }
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
