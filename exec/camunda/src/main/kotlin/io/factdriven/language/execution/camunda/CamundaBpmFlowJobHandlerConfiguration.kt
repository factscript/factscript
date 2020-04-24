package io.factdriven.language.execution.camunda

import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration

data class CamundaBpmFlowJobHandlerConfiguration(var message: String = ""): JobHandlerConfiguration {

    override fun toCanonicalString(): String? {
        return message
    }

}