package io.factdriven.language.execution.camunda

import io.factdriven.execution.Messages
import io.factdriven.execution.Message
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity

class CamundaBpmFlowJobHandler:
    JobHandler<CamundaBpmFlowJobHandlerConfiguration> {

    override fun getType(): String {
        return TYPE
    }

    override fun execute(
        configuration: CamundaBpmFlowJobHandlerConfiguration?,
        execution: ExecutionEntity?,
        commandContext: CommandContext?,
        tenantId: String?
    ) {
        Messages.process(
            Message.fromJson(
                configuration!!.message
            )
        )
    }

    override fun newConfiguration(canonicalString: String?): CamundaBpmFlowJobHandlerConfiguration {
        return CamundaBpmFlowJobHandlerConfiguration(
            canonicalString!!
        )
    }

    override fun onDelete(configuration: CamundaBpmFlowJobHandlerConfiguration?, jobEntity: JobEntity?) {
        //
    }

    companion object {

        const val TYPE = "flowJobHandler"

    }

}