package io.factdriven.language.definition.handle_failure

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
                    emit event PaymentFailed::class by { PaymentFailed(total) }
                }

                emit event PaymentRetrieved::class by { PaymentRetrieved(total) }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
data class PaymentFailed(val amount: Float)
