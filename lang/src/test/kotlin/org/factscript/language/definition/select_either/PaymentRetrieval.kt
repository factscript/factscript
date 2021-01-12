package org.factscript.language.definition.select_either

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

                on command RetrievePayment::class

                select("Payment (partly) uncovered?") either {
                    given ("Yes") condition { covered < total }
                    execute command { ChargeCreditCard(id, total - covered) }
                } or {
                    otherwise ("No")
                }

                emit event { PaymentRetrieved(total) }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
