package io.factdriven.flow.exec

import io.factdriven.flow.execute
import io.factdriven.flow.lang.FlowExecution
import io.factdriven.flow.lang.FlowMessagePayload
import io.factdriven.flow.view.transform
import io.factdriven.flow.view.translate
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CamundaBpmExecutionTest {

    val configuration = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
    val engine = configuration.buildProcessEngine()

    @Test
    fun testPaymentRetrievalVersion1() {

        val flow = execute <PaymentRetrieval> {

            on message (RetrievePayment::class) create acceptance ("PaymentRetrievalAccepted") by {
                PaymentRetrievalAccepted(paymentId = it.id)
            }

            execute service {
                create intent ("ChargeCreditCard") by { ChargeCreditCard() }
                on message CreditCardCharged::class create success("CreditCardCharged")
            }

            create success ("PaymentRetrieved") by { PaymentRetrieved() }

        }

        val bpmn = bpmn(flow)

        open(bpmn)

        val deployment = engine.repositoryService
            .createDeployment()
            .addModelInstance("${flow.name}.bpmn", bpmn)
            .name(flow.name)
            .deploy()

        Assertions.assertEquals(flow.name, deployment.name)

        val processDefinition = engine.repositoryService.createProcessDefinitionQuery().singleResult()

        Assertions.assertEquals("PaymentRetrieval", processDefinition.key)

        Mocks.register("flow", JavaDelegate {
            println(it.eventName + ":" + it.currentActivityId)
        })

        engine.runtimeService.correlateMessage("RetrievePayment")

        val processInstance = engine.runtimeService.createProcessInstanceQuery().singleResult()

        Assertions.assertNotNull(processInstance)

    }

    fun correlate(message: FlowMessagePayload) {

    }

    fun open(bpmnModelInstance: BpmnModelInstance) {
        val file = File.createTempFile("./bpmn-model-api-", ".bpmn")
        Bpmn.writeModelToFile(file, bpmnModelInstance)
        if("Mac OS X" == System.getProperty("os.name"))
            Runtime.getRuntime().exec("open " + file.absoluteFile);
    }

    fun bpmn(flow: FlowExecution<*>): BpmnModelInstance {
        val container = translate(flow)
        val bpmnModelInstance = transform(container)
        Bpmn.validateModel(bpmnModelInstance);
        return bpmnModelInstance
    }

}

data class PaymentRetrieval(val init: RetrievePayment) {

    val paymentId = init.id
    val accountId = init.accountId
    var uncovered = init.payment
    var covered = 0F

}

class RetrievePayment(val id: String, val accountId: String, val payment: Float)
class NotifyCustomer(val id: String? = null, val accountId: String? = null, val payment: Float? = null)
class CustomerNotified(val id: String, val accountId: String, val payment: Float)
class PaymentRetrievalAccepted(val paymentId: String? = null)
class PaymentRetrieved(val paymentId: String? = null)
class PaymentFailed(val paymentId: String)
class PaymentCoveredManually(val paymentId: String)
class ChargeCreditCard(val reference: String? = null, val payment: Float? = null)
class CreditCardCharged(val reference: String)
class CreditCardExpired(val reference: String)
class CreditCardDetailsUpdated(val reference: String)
class WithdrawAmount(val reference: String, val payment: Float)
class CreditAmount(val reference: String)
class AmountWithdrawn(val reference: String)
