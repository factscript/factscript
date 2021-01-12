package org.factscript.language.visualization.bpmn.model.issue_command

import org.factscript.language.*
import org.factscript.language.visualization.bpmn.model.BpmnModel
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    init {
        Flows.activate(PaymentRetrieval::class)
    }

    @Test
    fun testView() {
        BpmnModel(Flows.get(PaymentRetrieval::class)).toTempFile(true)
    }

}