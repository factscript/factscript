package io.factdriven.language.execution.camunda

import io.factdriven.language.Flows
import io.factdriven.language.visualization.bpmn.model.BpmnModel
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin

class CamundaFlowExecutionPlugin: ProcessEnginePlugin {

    override fun preInit(configuration: ProcessEngineConfigurationImpl) {
        configuration.customJobHandlers = configuration.customJobHandlers ?: mutableListOf()
        configuration.processEnginePlugins = configuration.processEnginePlugins + SpinProcessEnginePlugin()
        configuration.customJobHandlers.add(CamundaBpmFlowJobHandler())
        configuration.beans = mapOf("condition" to CamundaCondition())
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