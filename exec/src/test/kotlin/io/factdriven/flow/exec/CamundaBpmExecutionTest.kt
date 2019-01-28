package io.factdriven.flow.exec

import io.factdriven.flow.define
import io.factdriven.flow.lang.*
import io.factdriven.flow.past
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

        on message(RetrievePayment::class) create acceptance(PaymentRetrievalAccepted::class) by {
            PaymentRetrievalAccepted(paymentId = it.id)
        }

        execute service {
            create intent(ChargeCreditCard::class) by { ChargeCreditCard(reference = paymentId, payment = uncovered) }
            on message(CreditCardCharged::class) having "reference" match { paymentId } create success()
        }

        create success(PaymentRetrieved::class) by {
            PaymentRetrieved(paymentId = paymentId, payment = uncovered)
        }

    }

    @Test
    fun testPaymentRetrieval() {

        assertEquals(0, messages.size)
        assertNull(processInstance())

        correlate(RetrievePayment(id = "anId", accountId = "anAccountId", payment = 3F))

        assertEquals(3, messages.size)
        assertEquals(RetrievePayment::class, messages[0].fact::class)
        assertEquals(PaymentRetrievalAccepted::class, messages[1].fact::class)
        assertEquals(ChargeCreditCard::class, messages[2].fact::class)
        assertNotNull(processInstance())

        correlate(CreditCardCharged(reference = "anId"))

        assertEquals(5, messages.size)
        assertEquals(CreditCardCharged::class, messages[3].fact::class)
        assertEquals(PaymentRetrieved::class, messages[4].fact::class)
        assertNull(processInstance())

        val paymentRetrieval = past(messages.map { it.fact }, PaymentRetrieval::class)!!

        assertEquals(3F, paymentRetrieval.covered)
        assertEquals(0F, paymentRetrieval.uncovered)

    }

    private val messages = mutableListOf<Message<*>>()

    private fun processInstance(): ProcessInstance? {
        return engine.runtimeService.createProcessInstanceQuery().singleResult()
    }

    private fun correlate(fact: Fact) {
        val message = Message.createFrom(fact)
        val hash = paymentRetrieval.patterns(fact!!).iterator().next().hash
        val externalTasks = engine.externalTaskService.fetchAndLock(Int.MAX_VALUE, hash).topic(hash, Long.MAX_VALUE).execute()
        if (externalTasks != null && !externalTasks.isEmpty()) {
            externalTasks.forEach {
                messages.add(message) // TODO save in process instance
                engine.externalTaskService.complete(it.id, hash)
            }
        } else {
            val subscriptions = engine.runtimeService.createEventSubscriptionQuery().eventType(EventType.MESSAGE.name()).eventName(hash).list()
            if (subscriptions != null && ! subscriptions.isEmpty()) {
                subscriptions.forEach {
                    messages.add(message) // TODO save in process instance
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
            .addModelInstance("${paymentRetrieval.name}.bpmn", bpmn)
            .name(paymentRetrieval.name)
            .deploy()

        assertEquals(paymentRetrieval.name, deployment.name)

        val processDefinition = engine.repositoryService.createProcessDefinitionQuery().singleResult()

        assertEquals(paymentRetrieval.name, processDefinition.key)

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
                is FlowActionDefinition -> {
                    val action = element.function
                    if (action != null) {
                        val aggregate = past(messages.map { it.fact }, PaymentRetrieval::class)
                        messages.add(Message.createFrom(action.invoke(aggregate!!)))
                    }
                }
                is FlowMessageReactionDefinition -> {
                    val action = element.action?.function // TODO properly retrieve intent creator
                    if (action != null) {
                        val aggregate = past(messages.map { it.fact }, PaymentRetrieval::class)
                        val fact = action.invoke(aggregate!!, messages.last().fact)
                        messages.add(Message.createFrom(fact))
                    }
                    val aggregate = past(messages.map { it.fact }, PaymentRetrieval::class)
                    val message = element.expected(aggregate)
                    it.setVariable("data", message.hash)
                    println("Message: ${message.hash}")
                }
                is FlowDefinition -> {
                    val createIntentElement = element.children.first() // TODO properly retrieve intent creator
                    if (createIntentElement is FlowActionDefinition) {
                        val action = createIntentElement.function
                        if (action != null) {
                            val aggregate = past(messages.map { it.fact }, PaymentRetrieval::class)
                            messages.add(Message.createFrom(action.invoke(aggregate!!)))
                        }
                    }
                    val createSuccessElement = element.children.last() // TODO properly retrieve success listener
                    if (createSuccessElement is FlowMessageReactionDefinition) {
                        val aggregate = past(messages.map { it.fact }, PaymentRetrieval::class)
                        val message = createSuccessElement.expected(aggregate)
                        it.setVariable("data", message.hash)
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

data class RetrievePayment(val id: String, val accountId: String, val payment: Float)
data class NotifyCustomer(val id: String? = null, val accountId: String? = null, val payment: Float? = null)
data class CustomerNotified(val id: String, val accountId: String, val payment: Float)
data class PaymentRetrievalAccepted(val paymentId: String? = null)
data class PaymentRetrieved(val paymentId: String? = null, val payment: Float? = null)
data class PaymentFailed(val paymentId: String)
data class PaymentCoveredManually(val paymentId: String, val payment: Float? = null)
data class ChargeCreditCard(val reference: String? = null, val payment: Float? = null)
data class CreditCardCharged(val reference: String)
data class CreditCardExpired(val reference: String)
data class CreditCardDetailsUpdated(val reference: String)
data class WithdrawAmount(val reference: String, val payment: Float)
data class CreditAmount(val reference: String)
data class AmountWithdrawn(val reference: String)
