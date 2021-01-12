package org.factscript.language.visualization.bpmn.model.await_time

import org.factscript.language.*
import org.factscript.language.visualization.bpmn.model.BpmnModel
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CreditCardChargeTest {

    init {
        Flows.activate(CreditCardCharge::class)
    }

    @Test
    fun testDefinition() {
        BpmnModel(Flows.get(CreditCardCharge::class)).toTempFile(true)
    }

}