package io.factdriven.flow.exec

import io.factdriven.flow.camunda.CamundaBpmFlowBehaviour
import io.factdriven.flow.camunda.CamundaBpmFlowExecutor
import io.factdriven.flow.camunda.CamundaBpmFlowExecutor.correlate
import io.factdriven.flow.camunda.CamundaBpmFlowJobHandler
import io.factdriven.flow.lang.*
import io.factdriven.flow.view.transform
import io.factdriven.flow.view.translate
import org.apache.ibatis.logging.LogFactory
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor
import org.camunda.bpm.engine.impl.test.TestHelper
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.Thread.sleep


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CamundaBpmExecutionTest {

    var id: String? = null

    @Test
    fun testPaymentRetrieval() {

        correlate(RetrievePayment(reference = "anOrderId", accountId = "anAccountId", payment = 3F))

        assertEquals(3F, paymentRetrieval().covered)

    }

    private fun correlate(fact: Fact) {

        val message = Message.from(fact)

        if (id == null)
            id = message.id

        CamundaBpmFlowExecutor.target(message).map {
            CamundaBpmFlowExecutor.correlate(it)
        }

        TestHelper.waitForJobExecutorToProcessAllJobs(
            engine!!.processEngineConfiguration as ProcessEngineConfigurationImpl,
            60000,
            250
        )

    }

    private fun paymentRetrieval(): PaymentRetrieval {

        return CamundaBpmFlowExecutor.load(id!!, PaymentRetrieval::class)

    }

    init {

        LogFactory.useSlf4jLogging();

        PaymentRetrieval.init()
        CreditCardCharge.init()

        FlowDefinitions.all().forEach { flowDefinition ->
            val bpmn = bpmn(flowDefinition)
            engine().repositoryService
                .createDeployment()
                .addModelInstance("${flowDefinition.name}.bpmn", bpmn)
                .name(flowDefinition.name)
                .deploy()
            open(bpmn)
        }

        mock()

    }

    private var engine: ProcessEngine? = null

    private fun engine(): ProcessEngine {
        if (engine == null) {
            val configuration = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration() as ProcessEngineConfigurationImpl
            configuration.processEnginePlugins = listOf(SpinProcessEnginePlugin())
            configuration.customJobHandlers = listOf(CamundaBpmFlowJobHandler())
            //configuration.jobExecutor = DefaultJobExecutor()
            //configuration.jobExecutor.maxWait = 1000
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

}
