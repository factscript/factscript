package io.factdriven.language.definition.await_time

import io.factdriven.language.flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.*

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

                on time cycle ("Once a year") { "P1Y" } times 3 from { now() } limit { nikolaus2030 }

                execute command ChargeCreditCard::class by {
                    ChargeCreditCard(reference, charge)
                } but {
                    on time duration ("30 seconds") { "PT30S" } from { now() }
                    emit event ChargingProcessFailed::class by {
                        ChargingProcessFailed()
                    }
                }

                await time limit ("Nikolaus 2030") { nikolaus2030 }

                emit event CreditCardCharged::class by {
                    CreditCardCharged(reference, charge)
                }

            }

        }

    }

}

data class ChargeCreditCard(val reference: String, val charge: Float)
data class ConfirmationReceived(val reference: String)
data class CreditCardCharged(val reference: String, val charge: Float? = null)
class ChargingProcessFailed
