package io.factdriven.language.visualization.bpmn.model.start_and_end_event

import io.factdriven.language.Flows
import io.factdriven.language.visualization.bpmn.model.BpmnModel
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