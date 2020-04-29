package io.factdriven.language.visualization.bpmn.model.execute_command

import io.factdriven.language.Flows
import io.factdriven.language.visualization.bpmn.model.BpmnModel
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testView() {
        BpmnModel(Flows.initialize(CreditCardCharge::class, PaymentRetrieval::class)[1]).toTempFile(true)
    }

}