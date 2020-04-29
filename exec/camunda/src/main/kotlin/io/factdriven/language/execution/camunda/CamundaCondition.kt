package io.factdriven.language.execution.camunda

import io.factdriven.language.definition.Conditional
import org.camunda.bpm.engine.delegate.DelegateExecution

class CamundaCondition {

    fun evaluate(execution: DelegateExecution, id: String): Boolean {
        return execution.node<Conditional>(id).condition!!.invoke(execution.state)
    }

}