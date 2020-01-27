package io.factdriven.play

import io.factdriven.play.camunda.*

import org.apache.ibatis.logging.LogFactory
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.test.TestHelper
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import java.io.File

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
open class PlayUsingCamundaTest {

    private var engine: ProcessEngine? = null
        get() {
            if (field == null) {
                val configuration = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration() as ProcessEngineConfigurationImpl
                configuration.processEnginePlugins = listOf(CamundaFlowExecutionPlugin())
                configuration.isJobExecutorActivate = true
                field = configuration.buildProcessEngine()
            }
            return field
        }

    init {

        LogFactory.useSlf4jLogging();

    }

    protected fun send(fact: Any): String {

        engine

        val message = when(fact) {
            is Fact<*> -> Message(fact)
            is Message -> fact
            else -> Message(Fact(fact))
        }

        Player.publish(message)

        TestHelper.waitForJobExecutorToProcessAllJobs(
            engine?.processEngineConfiguration as ProcessEngineConfigurationImpl,
            60000,
            250
        )

        return message.id

    }

}