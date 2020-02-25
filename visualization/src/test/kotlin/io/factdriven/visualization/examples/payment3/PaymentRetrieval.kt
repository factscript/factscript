package io.factdriven.visualization.examples.payment3

import io.factdriven.language.define
import java.util.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(fact: RetrievePayment) {

    var id = UUID.randomUUID().toString()
    var total = fact.amount

    companion object {

        init {

            define <PaymentRetrieval> {

                on command RetrievePayment::class

                execute command ChargeCreditCard::class by {
                    ChargeCreditCard(id, total)
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
