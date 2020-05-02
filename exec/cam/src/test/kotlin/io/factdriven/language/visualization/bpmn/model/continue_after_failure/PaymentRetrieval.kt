package io.factdriven.language.visualization.bpmn.model.continue_after_failure

import io.factdriven.language.flow
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

                on command RetrievePayment::class promise {
                    report success PaymentRetrieved::class
                    report failure PaymentFailed::class
                }

                execute command ChargeCreditCard::class by {
                    ChargeCreditCard(id, total)
                } but {
                    on event CreditCardExpired::class
                    emit event PaymentFailed::class by { PaymentFailed() }
                } but {
                    on event CreditCardExpired::class
                    execute command ChargeCreditCard::class by {
                        ChargeCreditCard(id, total)
                    }
//                    emit event PaymentFailed::class by { PaymentFailed() }
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
class PaymentFailed
