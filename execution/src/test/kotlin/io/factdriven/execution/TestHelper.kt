package io.factdriven.execution

import io.factdriven.execution.camunda.*

import org.apache.ibatis.logging.LogFactory
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.test.TestHelper
import org.junit.jupiter.api.BeforeEach
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
            Player.register(CamundaRepository())
            Player.register(CamundaPublisher())
            Player.register(CamundaProcessor())
        }

    }

    init {

        LogFactory.useSlf4jLogging();

    }

    protected fun send(type: KClass<*>, fact: Any): String {

        val message = when(fact) {
            is Fact<*> -> Message.from(type, fact)
            is Message -> fact
            else -> Message.from(type, Fact(fact))
        }

        Player.publish(message)

        TestHelper.waitForJobExecutorToProcessAllJobs(
            engine.processEngineConfiguration as ProcessEngineConfigurationImpl,
            60000,
            250
        )

        return message.fact.id

    }

    @BeforeEach
    fun createDeployment() {
        plugin.postProcessEngineBuild(engine)
    }

}