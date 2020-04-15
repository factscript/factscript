package io.factdriven.execution.camunda.model.payment3

import io.factdriven.Flows
import io.factdriven.execution.camunda.model.BpmnModel
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