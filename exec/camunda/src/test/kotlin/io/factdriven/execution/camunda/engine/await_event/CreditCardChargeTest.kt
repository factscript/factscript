package io.factdriven.execution.camunda.engine.await_event

import io.factdriven.Flows
import io.factdriven.execution.camunda.engine.TestHelper
import io.factdriven.execution.load
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest: TestHelper() {

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