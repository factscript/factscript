package org.factscript.language.execution.cam.loop

import org.factscript.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(fact: RetrievePayment) {

    val reference = "SomeReference"
    val amount = fact.amount
    var charged = 0F
    var retrieved = false; private set

    fun apply(fact: PaymentRetrieved) {
        retrieved = true
    }

    fun apply(fact: CreditCardCharged) {
        charged += fact.amount
    }

    companion object {

        init {

            flow <PaymentRetrieval> {

                on command RetrievePayment::class

                repeat {
                    execute command { ChargeCreditCard(reference, 1F) }
                    until ("Payment charged?") condition { charged == amount }
                }

                emit event { PaymentRetrieved(amount) }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
