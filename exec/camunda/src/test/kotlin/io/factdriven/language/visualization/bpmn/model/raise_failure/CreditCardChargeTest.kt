package io.factdriven.language.visualization.bpmn.model.raise_failure

import io.factdriven.language.Flows
import io.factdriven.language.visualization.bpmn.model.BpmnModel
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest {

    @Test
    fun testView() {
        Flows.initialize(CreditCardCharge::class)
        BpmnModel(Flows.get(CreditCardCharge::class)).toTempFile(true)
    }

}