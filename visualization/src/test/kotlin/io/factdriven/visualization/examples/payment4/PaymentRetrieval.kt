package io.factdriven.visualization.examples.payment4

import io.factdriven.language.define
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

            define <PaymentRetrieval> {

                on command RetrievePayment::class

                select ("Payment (partly) uncovered?") either {
                    given ("Yes") condition { covered < total }
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(id, total - covered)
                    }
                } or {
                    given ("Third") // = default path w/o condition
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(id, total - covered)
                    }
                } or {
                    given ("No") // = default path w/o condition
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
