package io.factdriven.language.visualization.bpmn.model.raise_failure

import io.factdriven.language.flow
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
                    given("Yes") //condition { covered < total }
                } or {
                    given("No") condition { covered < total } // = default path w/o condition
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
