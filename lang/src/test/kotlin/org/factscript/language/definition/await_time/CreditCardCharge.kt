package org.factscript.language.definition.await_time

import org.factscript.language.*
import java.time.LocalDate

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardCharge(fact: ChargeCreditCard) {

    val reference = fact.reference
    val charge = fact.charge
    var nikolaus2030 = LocalDate.of(2030, 12, 6).atStartOfDay()

    companion object {

        init {

            flow <CreditCardCharge> {

                on command ChargeCreditCard::class emit {
                    success event CreditCardCharged::class
                    failure event ChargingProcessFailed::class
                }

                execute command {
                    ChargeCreditCard(reference, charge)
                } but {
                    on time duration ("30 seconds") { "PT30S" }
                    emit failure event { ChargingProcessFailed() }
                }

                await time limit ("Nikolaus 2030") { nikolaus2030 }

                emit success event { CreditCardCharged(reference, charge) }

            }

        }

    }

}

data class ChargeCreditCard(val reference: String, val charge: Float)
data class ConfirmationReceived(val reference: String)
data class CreditCardCharged(val reference: String, val charge: Float? = null)
class ChargingProcessFailed
