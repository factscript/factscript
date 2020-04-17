package io.factdriven.execution.camunda.model.start_and_end_event

import io.factdriven.Flows
import io.factdriven.execution.camunda.model.BpmnModel
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testView() {
        BpmnModel(Flows.get(PaymentRetrieval::class)).toTempFile(true)
    }

}