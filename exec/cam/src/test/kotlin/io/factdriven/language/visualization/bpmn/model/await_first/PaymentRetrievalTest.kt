package io.factdriven.language.visualization.bpmn.model.await_first

import io.factdriven.language.Flows
import io.factdriven.language.visualization.bpmn.model.BpmnModel
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testView() {
        Flows.initialize(PaymentRetrieval::class, CreditCardCharge::class)
        BpmnModel(Flows.get(PaymentRetrieval::class)).toTempFile(true)
    }

}