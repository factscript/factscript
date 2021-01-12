package org.factscript.language.visualization.bpmn.model.select_all

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

                select all {
                    given("A") condition { covered < total }
                    execute command {
                        ChargeCreditCard(
                            id,
                            total - covered
                        )
                    }
                } or {
                    given("B") condition { covered < total }
                    execute command {
                        ChargeCreditCard(
                            id,
                            total - covered
                        )
                    }
                } or {
                    given("C") // = default path w/o condition
                    execute command {
                        ChargeCreditCard(
                            id,
                            total - covered
                        )
                    }
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
