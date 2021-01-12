package org.factscript.language.execution.cam

import org.factscript.execution.Messages
import org.factscript.execution.Fact
import org.factscript.execution.Message

import org.apache.ibatis.logging.LogFactory
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.test.TestHelper
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
open class TestHelper {

    companion object {

        private val plugin = FactscriptLanguagePlugin()
        private val engine: ProcessEngine

        init {
            val configuration = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration() as ProcessEngineConfigurationImpl
            configuration.processEnginePlugins = listOf(FactscriptLanguagePlugin())
            configuration.isJobExecutorActivate = true
            configuration.beans = mapOf("condition" to EngineCondition())
            engine = configuration.buildProcessEngine()
            Messages.register(EngineMessageStore())
            Messages.register(EngineMessagePublisher())
            Messages.register(EngineMessageProcessor())
        }

    }

    init {

        LogFactory.useSlf4jLogging();

    }

    protected fun send(type: KClass<*>, fact: Any): String {

        plugin.postProcessEngineBuild(engine)

        val message = when(fact) {
            is Fact<*> -> Message(type, fact)
            is Message -> fact
            else -> Message(type, Fact(fact))
        }

        Messages.publish(message)
        sleep(60)

        return message.fact.id

    }

    protected fun sleep(seconds: Long, interval: Long = 250) {
        TestHelper.waitForJobExecutorToProcessAllJobs(engine.processEngineConfiguration as ProcessEngineConfigurationImpl, seconds * 1000, interval)
    }

}