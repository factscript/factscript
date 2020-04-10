package io.factdriven.impl.execution.examples.creditcard1

import io.factdriven.Flows
import io.factdriven.impl.execution.PlayUsingCamundaTest
import io.factdriven.impl.execution.load
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest: PlayUsingCamundaTest() {

    init {
        Flows.initialize(CreditCardCharge::class)
    }

    @Test
    fun test() {

        val id = send(CreditCardCharge::class, ChargeCreditCard(reference = "anOrderId", charge = 5F))
        var charge = CreditCardCharge::class.load(id)
        Assertions.assertEquals(5F, charge.charge)
        Assertions.assertEquals(false, charge.closed)
        send(CreditCardCharge::class, ConfirmationReceived(reference = "anOrderId"))
        charge = CreditCardCharge::class.load(id)
        Assertions.assertEquals(true, charge.closed)

    }

}