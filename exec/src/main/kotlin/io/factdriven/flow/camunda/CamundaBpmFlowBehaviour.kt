package io.factdriven.flow.camunda

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

object FlowServiceBehaviour : AbstractBpmnActivityBehavior() {

    override fun execute(execution: ActivityExecution) {
        //
    }

    override fun signal(execution: ActivityExecution, signalName: String?, signalData: Any?) {
        leave(execution)
        // propagateBpmnError(if (signalData !is String) BpmnError(signalName) else BpmnError(signalName, signalData), execution)
    }

}

object FlowActionBehaviour : JavaDelegate {

    override fun execute(execution: DelegateExecution) {
        //
    }

}
