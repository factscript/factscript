package io.factdriven.execution.examples.creditcard1

import io.factdriven.definition.Definition
import io.factdriven.definition.Definitions
import io.factdriven.execution.PlayUsingCamundaTest
import io.factdriven.execution.Player
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest: PlayUsingCamundaTest() {

    init {
        Definitions.clear()
        Definitions.init(CreditCardCharge::class)
    }

    @Test
    fun test() {

        val id = send(CreditCardCharge::class, ChargeCreditCard(reference = "anOrderId", charge = 5F))
        var charge = Player.load(id, CreditCardCharge::class)
        Assertions.assertEquals(5F, charge.charge)
        Assertions.assertEquals(false, charge.closed)
        send(CreditCardCharge::class, ConfirmationReceived(reference = "anOrderId"))
        charge = Player.load(id, CreditCardCharge::class)
        Assertions.assertEquals(true, charge.closed)

    }

}