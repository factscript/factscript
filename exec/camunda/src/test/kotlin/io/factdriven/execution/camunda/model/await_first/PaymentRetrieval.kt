package io.factdriven.execution.camunda.model.await_first

import io.factdriven.flow
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
                    on event ThisMessage::class
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(
                            id,
                            total - covered
                        )
                    }
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(
                            id,
                            total - covered
                        )
                    }
                } or {
                    on event ThatMessage::class
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(
                            id,
                            total - covered
                        )
                    }
                }

                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(total)
                }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
data class ThisMessage(val amount: Float)
data class ThatMessage(val amount: Float)
