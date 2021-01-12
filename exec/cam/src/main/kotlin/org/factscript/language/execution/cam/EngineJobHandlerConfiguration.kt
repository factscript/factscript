package org.factscript.language.execution.cam

import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration

data class EngineJobHandlerConfiguration(var message: String = ""): JobHandlerConfiguration {

    override fun toCanonicalString(): String? {
        return message
    }

}