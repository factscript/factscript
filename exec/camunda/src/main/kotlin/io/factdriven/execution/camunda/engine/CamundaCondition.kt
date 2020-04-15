package io.factdriven.execution.camunda.engine

import io.factdriven.definition.Conditional
import org.camunda.bpm.engine.delegate.DelegateExecution

class CamundaCondition {

    fun evaluate(execution: DelegateExecution, id: String): Boolean {
        return execution.node<Conditional>(id).condition!!.invoke(execution.state)
    }

}