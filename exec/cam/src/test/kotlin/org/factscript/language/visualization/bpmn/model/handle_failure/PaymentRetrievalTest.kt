package org.factscript.language.visualization.bpmn.model.handle_failure

import org.factscript.language.*
import org.factscript.language.visualization.bpmn.model.BpmnModel
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    @Test
    fun testView() {
        Flows.activate(CreditCardCharge::class, PaymentRetrieval::class)
        BpmnModel(Flows.get(CreditCardCharge::class)).toTempFile(true)
        BpmnModel(Flows.get(PaymentRetrieval::class)).toTempFile(true)
    }

}