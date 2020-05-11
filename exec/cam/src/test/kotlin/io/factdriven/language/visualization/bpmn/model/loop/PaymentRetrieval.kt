package io.factdriven.language.visualization.bpmn.model.loop

import io.factdriven.language.*
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

                loop {
                    loop {
                        loop {
                            execute command {
                                ChargeCreditCard(
                                    id,
                                    1F
                                )
                            }
                            until("ABC") condition { covered == total }
                        }
                        execute command {
                            ChargeCreditCard(
                                id,
                                1F
                            )
                        }
                        until("ABC") condition { covered == total }
                    }
                    until("Payment covered?") condition { covered == total }
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
