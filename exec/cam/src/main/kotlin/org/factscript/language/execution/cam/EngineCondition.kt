package org.factscript.language.execution.cam

import org.factscript.language.definition.*
import org.camunda.bpm.engine.delegate.DelegateExecution

class EngineCondition {

    fun evaluate(execution: DelegateExecution, id: String): Boolean {
        return execution.node<ConditionalNode>(id).condition!!.invoke(execution.state)
    }

}