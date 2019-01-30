package io.factdriven.flow.camunda

import io.factdriven.flow.lang.*
import io.factdriven.flow.view.transform
import io.factdriven.flow.view.translate
import org.apache.ibatis.logging.LogFactory
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.test.TestHelper
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin
import java.io.File
import kotlin.reflect.KClass

open class CamundaFlowExecutionTest {

    private var engine: ProcessEngine? = null

    protected fun send(message: Message<*>) {

        deploy()

        CamundaBpmFlowExecutor.target(message).map {
            CamundaBpmFlowExecutor.correlate(it)
        }

        TestHelper.waitForJobExecutorToProcessAllJobs(
            engine!!.processEngineConfiguration as ProcessEngineConfigurationImpl,
            60000,
            250
        )

    }

    protected fun <A: Aggregate> find(id: AggregateId, type: KClass<out A>): A {
        return CamundaBpmFlowExecutor.load(id, type)
    }

    protected fun engine(): ProcessEngine {
        if (engine == null) {
            val configuration = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration() as ProcessEngineConfigurationImpl
            configuration.processEnginePlugins = listOf(SpinProcessEnginePlugin())
            configuration.customJobHandlers = listOf(CamundaBpmFlowJobHandler())
            configuration.isJobExecutorActivate = true
            engine = configuration.buildProcessEngine()
        }
        return engine!!
    }

    fun open(bpmnModelInstance: BpmnModelInstance) {
        val file = File.createTempFile("./bpmn-model-api-", ".bpmn")
        Bpmn.writeModelToFile(file, bpmnModelInstance)
        if("Mac OS X" == System.getProperty("os.name"))
            Runtime.getRuntime().exec("open " + file.absoluteFile);
    }

    fun bpmn(flow: FlowDefinition<*>): BpmnModelInstance {
        val container = translate(flow)
        val bpmnModelInstance = transform(container)
        Bpmn.validateModel(bpmnModelInstance);
        return bpmnModelInstance
    }

    fun mock() {

        Mocks.register("start", CamundaBpmFlowBehaviour)

    }

    fun deploy() {

        if (engine!!.repositoryService.createDeploymentQuery().list().isEmpty()) {

            FlowDefinitions.all().forEach { flowDefinition ->
                val bpmn = bpmn(flowDefinition)
                engine().repositoryService
                    .createDeployment()
                    .addModelInstance("${flowDefinition.name}.bpmn", bpmn)
                    .name(flowDefinition.name)
                    .deploy()
                open(bpmn)
            }

        }

    }

    init {

        LogFactory.useSlf4jLogging();
        engine()
        mock()

    }


}