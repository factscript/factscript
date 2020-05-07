package io.factdriven.language.execution.cam

import io.factdriven.language.Flows
import io.factdriven.language.definition.Flow
import io.factdriven.language.visualization.bpmn.model.BpmnModel
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class FulfillmentTest {

    val flows: List<Flow>

    init {
        flows = Flows.initialize(
            Fulfillment::class,
            Payment::class,
            CreditCard::class
        )
    }

    @Test
    fun testDefinition() {
        flows.forEach { flow -> BpmnModel(flow).toTempFile(true) }
    }

}