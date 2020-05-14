package io.factdriven.language.execution.cam

import io.factdriven.execution.Message
import io.factdriven.execution.MessagePublisher
import io.factdriven.language.impl.utils.*
import org.camunda.bpm.engine.ProcessEngines
import org.camunda.bpm.engine.impl.ProcessEngineImpl
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity

class EngineMessagePublisher: MessagePublisher {

    private val engine: ProcessEngineImpl get() = ProcessEngines.getProcessEngines().values.first() as ProcessEngineImpl

    override fun publish(vararg message: Message) {
        message.forEach { m ->
            engine.processEngineConfiguration.commandExecutorTxRequired.execute(CreateCamundaBpmFlowJob(m))
        }
    }

    class CreateCamundaBpmFlowJob(private val message: Message) : Command<String> {

        override fun execute(commandContext: CommandContext): String {
            val job = MessageEntity()
            job.jobHandlerType = EngineJobHandler.TYPE
            job.jobHandlerConfiguration = EngineJobHandlerConfiguration(message.compactJson)
            Context.getCommandContext().jobManager.send(job)
            return job.id
        }

    }

}