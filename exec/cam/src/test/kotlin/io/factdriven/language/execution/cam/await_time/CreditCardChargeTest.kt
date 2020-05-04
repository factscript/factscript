package io.factdriven.language.execution.cam.await_time

import io.factdriven.language.Flows
import io.factdriven.language.execution.cam.TestHelper
import io.factdriven.execution.load
import io.factdriven.language.visualization.bpmn.model.BpmnModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest: TestHelper() {

    init {
        Flows.initialize(CreditCardCharge::class).forEach {
            BpmnModel(it).toTempFile(true)
        }
    }

    @Test
    fun testSuccess() {

        val id = send(CreditCardCharge::class, ChargeCreditCard(reference = "anOrderId", charge = 5F))
        var charge = CreditCardCharge::class.load(id)

        assertEquals(5F, charge.charge)
        assertEquals(true, charge.open)
        assertEquals(false, charge.failed)

        send(CreditCardCharge::class, ConfirmationReceived(reference = "anOrderId"))

        charge = CreditCardCharge::class.load(id)
        assertEquals(false, charge.open)
        assertEquals(false, charge.failed)

    }

    @Test
    fun testFailure() {

        val id = send(CreditCardCharge::class, ChargeCreditCard(reference = "anOrderId", charge = 5F))
        var charge = CreditCardCharge::class.load(id)
        assertEquals(5F, charge.charge)
        assertEquals(true, charge.open)

        sleep(60, 5000)

        charge = CreditCardCharge::class.load(id)
        assertEquals(false, charge.open)
        assertEquals(true, charge.failed)

    }

}