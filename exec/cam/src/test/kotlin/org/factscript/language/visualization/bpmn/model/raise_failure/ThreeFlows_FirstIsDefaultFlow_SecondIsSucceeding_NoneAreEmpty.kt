package org.factscript.language.visualization.bpmn.model.raise_failure

import org.factscript.language.*
import org.factscript.language.visualization.bpmn.model.BpmnModel
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class ThreeFlows_FirstIsDefaultFlow_SecondIsSucceeding_NoneAreEmpty {

    data class CreditCardCharge(val fact: ChargeCreditCard) {

        private val reference: String = fact.reference
        private val amount: Float = fact.amount
        private var successful: Boolean = false

        fun apply(fact: CreditCardCharged) {
            successful = true
        }

        companion object {

            init {

                flow <CreditCardCharge> {

                    on command ChargeCreditCard::class emit {
                        success event CreditCardCharged::class
                        failure event CreditCardExpired::class
                    }

                    await event (CreditCardGatewayConfirmationReceived::class) having "reference" match { reference }

                    select ("Credit card expired?") either {
                        given ("No")
                        execute command { ChargeCreditCard(reference, amount) }
                        emit event { CreditCardExpired(reference) }
                    } or {
                        given ("Yes") condition { true }
                        execute command { ChargeCreditCard(reference, amount) }
                        execute command { ChargeCreditCard(reference, amount) }
                    } or {
                        given ("Yes") condition { true }
                        execute command { ChargeCreditCard(reference, amount) }
                        execute command { ChargeCreditCard(reference, amount) }
                        emit event { CreditCardExpired(reference) }
                    }

                    emit event {
                        CreditCardCharged(
                            reference,
                            amount
                        )
                    }

                }

            }

        }

    }

    data class ChargeCreditCard(val reference: String, val amount: Float)
    data class CreditCardCharged(val reference: String, val amount: Float)
    data class CreditCardExpired(val reference: String)
    data class CreditCardGatewayConfirmationReceived(val reference: String, val amount: Float)

    @Test
    fun testView() {
        Flows.activate(CreditCardCharge::class)
        BpmnModel(Flows.get(CreditCardCharge::class)).toTempFile(true)
    }

}