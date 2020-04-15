package io.factdriven.execution.camunda.engine

import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration

data class CamundaBpmFlowJobHandlerConfiguration(var message: String = ""): JobHandlerConfiguration {

    override fun toCanonicalString(): String? {
        return message
    }

}