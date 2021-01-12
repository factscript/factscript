package org.factscript.language.execution.cam

import org.factscript.language.*
import org.factscript.language.visualization.bpmn.model.BpmnModel
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin

class FactscriptLanguagePlugin: ProcessEnginePlugin {

    override fun preInit(configuration: ProcessEngineConfigurationImpl) {
        configuration.customJobHandlers = configuration.customJobHandlers ?: mutableListOf()
        configuration.processEnginePlugins = configuration.processEnginePlugins + SpinProcessEnginePlugin()
        configuration.customJobHandlers.add(EngineJobHandler())
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