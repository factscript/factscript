package org.factscript.language.execution.cam.await_event

import org.factscript.language.*
import org.factscript.language.execution.cam.TestHelper
import org.factscript.execution.load
import org.factscript.language.visualization.bpmn.model.BpmnModel
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest: TestHelper() {

    init {
        Flows.activate(CreditCardCharge::class).forEach {
            BpmnModel(it).toTempFile(true)
        }
    }

    @Test
    fun test() {

        val id = send(
            CreditCardCharge::class,
            ChargeCreditCard(
                reference = "anOrderId",
                charge = 5F
            )
        )
        var charge = CreditCardCharge::class.load(id)
        Assertions.assertEquals(5F, charge.charge)
        Assertions.assertEquals(false, charge.closed)
        send(
            CreditCardCharge::class,
            ConfirmationReceived(reference = "anOrderId")
        )
        charge = CreditCardCharge::class.load(id)
        Assertions.assertEquals(true, charge.closed)

    }

}