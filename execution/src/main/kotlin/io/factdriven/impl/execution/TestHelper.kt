package io.factdriven.impl.execution

import io.factdriven.Messages
import io.factdriven.impl.execution.camunda.*

import org.apache.ibatis.logging.LogFactory
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.test.TestHelper
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
open class PlayUsingCamundaTest {

    companion object {

        private val plugin = CamundaFlowExecutionPlugin()
        private val engine: ProcessEngine

        init {
            val configuration = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration() as ProcessEngineConfigurationImpl
            configuration.processEnginePlugins = listOf(CamundaFlowExecutionPlugin())
            configuration.isJobExecutorActivate = true
            engine = configuration.buildProcessEngine()
            Messages.register(CamundaMessageStore())
            Messages.register(CamundaMessagePublisher())
            Messages.register(CamundaMessageProcessor())
        }

    }

    init {

        LogFactory.useSlf4jLogging();

    }

    protected fun send(type: KClass<*>, fact: Any): String {

        plugin.postProcessEngineBuild(
            engine
        )

        val message = when(fact) {
            is Fact<*> -> Message(
                type,
                fact
            )
            is Message -> fact
            else -> Message(
                type,
                Fact(fact)
            )
        }

        Messages.publish(message)

        TestHelper.waitForJobExecutorToProcessAllJobs(
            engine.processEngineConfiguration as ProcessEngineConfigurationImpl,
            60000,
            250
        )

        return message.fact.id

    }

}