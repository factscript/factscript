package io.factdriven.flow.exec

import io.factdriven.flow.define
import io.factdriven.flow.lang.*
import io.factdriven.flow.view.transform
import io.factdriven.flow.view.translate
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.impl.event.EventType
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class CamundaBpmExecutionTest {

    private val paymentRetrieval = define <PaymentRetrieval> {

        on message (RetrievePayment::class) create acceptance ("PaymentRetrievalAccepted") by {
            PaymentRetrievalAccepted(paymentId = it.id)
        }

        execute service {
            create intent ("ChargeCreditCard") by { ChargeCreditCard() }
            on message CreditCardCharged::class having "reference" match { paymentId } create success("CreditCardCharged")
        }

        create success ("PaymentRetrieved") by { PaymentRetrieved() }

    }

    private val incoming = listOf(
        RetrievePayment(id = "anId", accountId = "anAccountId", payment = 3F),
        CreditCardCharged(reference = "anId")
    ).map{ it::class to it }.toMap()

    private val messages = mutableListOf<Message>()

    @Test
    fun testPaymentRetrieval() {

        assertNull(processInstance())

        correlate(incoming[RetrievePayment::class])

        assertNotNull(processInstance())

        correlate(incoming[CreditCardCharged::class])

        assertNull(processInstance())

    }

    private fun processInstance(): ProcessInstance? {
        return engine.runtimeService.createProcessInstanceQuery().singleResult()
    }

    private fun correlate(message: Message?) {
        val hash = paymentRetrieval.patterns(message!!).iterator().next().hash
        val externalTasks = engine.externalTaskService.fetchAndLock(Int.MAX_VALUE, hash).topic(hash, Long.MAX_VALUE).execute()
        if (externalTasks != null && !externalTasks.isEmpty()) {
            externalTasks.forEach {
                engine.externalTaskService.complete(it.id, hash)
            }
        } else {
            val subscriptions = engine.runtimeService.createEventSubscriptionQuery().eventType(EventType.MESSAGE.name()).eventName(hash).list()
            if (subscriptions != null && ! subscriptions.isEmpty()) {
                subscriptions.forEach {
                    val correlationBuilder = engine.runtimeService.createMessageCorrelation(hash)
                    if (it.processInstanceId != null)
                        correlationBuilder.processInstanceId(it.processInstanceId)
                    correlationBuilder.correlate()
                }
            }
        }
    }


    private val engine = engine()

    init {

        val bpmn = bpmn(paymentRetrieval)

        open(bpmn)

        val deployment = engine.repositoryService
            .createDeployment()
            .addModelInstance("${paymentRetrieval.flowElementType}.bpmn", bpmn)
            .name(paymentRetrieval.flowElementType)
            .deploy()

        assertEquals(paymentRetrieval.flowElementType, deployment.name)

        val processDefinition = engine.repositoryService.createProcessDefinitionQuery().singleResult()

        assertEquals(paymentRetrieval.flowElementType, processDefinition.key)

        mock()

    }

    private fun engine(): ProcessEngine {
        return ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine()
    }

    fun open(bpmnModelInstance: BpmnModelInstance) {
        val file = File.createTempFile("./bpmn-model-api-", ".bpmn")
        Bpmn.writeModelToFile(file, bpmnModelInstance)
        if("Mac OS X" == System.getProperty("os.name"))
            Runtime.getRuntime().exec("open " + file.absoluteFile);
    }

    fun bpmn(flow: FlowDefinition): BpmnModelInstance {
        val container = translate(flow)
        val bpmnModelInstance = transform(container)
        Bpmn.validateModel(bpmnModelInstance);
        return bpmnModelInstance
    }

    fun mock() {

        Mocks.register("enter", JavaDelegate {
            println("Enter ${it.currentActivityId}")
            val element = paymentRetrieval.descendantMap[it.currentActivityId]
            when (element) {
                is FlowMessageReactionDefinition -> {
                    val message = element.expected(null) // TODO properly reconstruct aggregate
                    it.setVariable("message", message.hash)
                    println("Message: ${message.hash}")
                }
                is FlowDefinition -> {
                    val listener = element.children.last() // TODO properly retrieve success listener
                    if (listener is FlowMessageReactionDefinition) {
                        val message = listener.expected(PaymentRetrieval(incoming[RetrievePayment::class] as RetrievePayment)) // TODO properly reconstruct aggregate
                        it.setVariable("message", message.hash)
                        println("Message: ${message.hash}")
                    }
                }
            }
        })

        Mocks.register("leave", JavaDelegate {
            println("Leave ${it.currentActivityId}")
        })

    }

}

class PaymentRetrieval(init: RetrievePayment) {

    val paymentId = init.id
    val accountId = init.accountId
    var uncovered = init.payment
    var covered = 0F

    fun apply(event: PaymentRetrieved) {
        covered += event.payment ?: uncovered
        uncovered -= event.payment ?: uncovered
    }

}

class RetrievePayment(val id: String, val accountId: String, val payment: Float)
class NotifyCustomer(val id: String? = null, val accountId: String? = null, val payment: Float? = null)
class CustomerNotified(val id: String, val accountId: String, val payment: Float)
class PaymentRetrievalAccepted(val paymentId: String? = null)
class PaymentRetrieved(val paymentId: String? = null, val payment: Float? = null)
class PaymentFailed(val paymentId: String)
class PaymentCoveredManually(val paymentId: String, val payment: Float? = null)
class ChargeCreditCard(val reference: String? = null, val payment: Float? = null)
class CreditCardCharged(val reference: String)
class CreditCardExpired(val reference: String)
class CreditCardDetailsUpdated(val reference: String)
class WithdrawAmount(val reference: String, val payment: Float)
class CreditAmount(val reference: String)
class AmountWithdrawn(val reference: String)
