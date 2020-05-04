package io.factdriven.language.visualization.bpmn.model.await_time

import io.factdriven.language.Flows
import io.factdriven.language.visualization.bpmn.model.BpmnModel
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest {

    @Test @Disabled
    fun testDefinition() {
        BpmnModel(Flows.get(CreditCardCharge::class)).toTempFile(true)
    }

}