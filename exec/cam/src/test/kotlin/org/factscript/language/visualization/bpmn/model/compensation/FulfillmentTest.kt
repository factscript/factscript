package org.factscript.language.visualization.bpmn.model.compensation

import org.factscript.language.*
import org.factscript.language.definition.Flow
import org.factscript.language.visualization.bpmn.model.BpmnModel
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class FulfillmentTest {

    val flows: List<Flow>

    init {
        flows = Flows.activate(
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