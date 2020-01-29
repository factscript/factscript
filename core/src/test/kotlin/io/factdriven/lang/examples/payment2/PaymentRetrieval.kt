package io.factdriven.lang.examples.payment2

import io.factdriven.lang.define
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

                issue command ChargeCreditCard::class by {
                    ChargeCreditCard(id, total)
                }

                consume event CreditCardCharged::class having "id" match { id }

                emit event PaymentRetrieved::class by {
                    PaymentRetrieved(total)
                }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
