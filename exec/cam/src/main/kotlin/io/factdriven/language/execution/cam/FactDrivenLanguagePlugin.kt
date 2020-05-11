package io.factdriven.language.execution.cam

import io.factdriven.language.*
import io.factdriven.language.visualization.bpmn.model.BpmnModel
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin

class FactDrivenLanguagePlugin: ProcessEnginePlugin {

    override fun preInit(configuration: ProcessEngineConfigurationImpl) {
        configuration.customJobHandlers = configuration.customJobHandlers ?: mutableListOf()
        configuration.processEnginePlugins = configuration.processEnginePlugins + SpinProcessEnginePlugin()
        configuration.customJobHandlers.add(EngineJobHandler())
        configuration.beans = mapOf("condition" to EngineCondition())
    }

    override fun postInit(configuration: ProcessEngineConfigurationImpl) {
        //
    }

    override fun postProcessEngineBuild(engine: ProcessEngine) {

        Flows.all().forEach { definition ->
            val modelInstance = BpmnModel(definition).toExecutable()
            engine.repositoryService
                .createDeployment()
                .addModelInstance("${definition.id}.bpmn", modelInstance)
                .name(definition.id)
                .deploy()
        }

    }

}