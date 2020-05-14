package io.factdriven.language.execution.cam

import io.factdriven.execution.Messages
import io.factdriven.execution.Message
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity

class EngineJobHandler: JobHandler<EngineJobHandlerConfiguration> {

    override fun getType(): String {
        return TYPE
    }

    override fun execute(configuration: EngineJobHandlerConfiguration?, execution: ExecutionEntity?, commandContext: CommandContext?, tenantId: String?) {
        Messages.process(Message.fromJson(configuration!!.message))
    }

    override fun newConfiguration(canonicalString: String?): EngineJobHandlerConfiguration {
        return EngineJobHandlerConfiguration(canonicalString!!)
    }

    override fun onDelete(configuration: EngineJobHandlerConfiguration?, jobEntity: JobEntity?) {
        //
    }

    companion object {
        const val TYPE = "flowJobHandler"
    }

}