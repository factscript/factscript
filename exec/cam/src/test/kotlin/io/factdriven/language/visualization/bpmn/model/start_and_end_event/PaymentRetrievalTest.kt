package io.factdriven.language.visualization.bpmn.model.start_and_end_event

import io.factdriven.language.*
import io.factdriven.language.visualization.bpmn.model.BpmnModel
import io.factdriven.language.visualization.bpmn.model.correct_continue_repeat_after_failure.*
import org.junit.jupiter.api.Test

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrievalTest {

    init { Flows.activate(PaymentRetrieval::class) }

    @Test
    fun testView() {
        BpmnModel(Flows.get(PaymentRetrieval::class)).toTempFile(true)
    }

}