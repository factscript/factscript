package io.factdriven.language.payment6

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

                execute all {
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(id, total - covered)
                    }
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(id, total - covered)
                    }
                } and {
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(id, total - covered)
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
