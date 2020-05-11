package io.factdriven.language.visualization.bpmn.model.await_event

import io.factdriven.language.*
import io.factdriven.language.visualization.bpmn.model.BpmnModel
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest {

    @Test
    fun testDefinition() {
        BpmnModel(Flows.get(CreditCardCharge::class)).toTempFile(true)
    }

}