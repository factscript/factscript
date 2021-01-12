package org.factscript.language.visualization.bpmn.model.nesting_gateways

import org.factscript.language.*
import org.factscript.language.visualization.bpmn.model.BpmnModel
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testView() {
        Flows.activate(PaymentRetrieval::class, CreditCardCharge::class)
        BpmnModel(Flows.get(PaymentRetrieval::class)).toTempFile(true)
    }

}