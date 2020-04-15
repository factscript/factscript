package io.factdriven.execution.camunda.model.creditcard1

import io.factdriven.Flows
import io.factdriven.execution.camunda.model.BpmnModel
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