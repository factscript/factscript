package io.factdriven.flow.camunda

import io.factdriven.flow.lang.Fact
import io.factdriven.flow.lang.FlowDefinitions
import io.factdriven.flow.lang.Message
import io.factdriven.flow.view.BpmnEventType
import io.factdriven.flow.view.transform
import io.factdriven.flow.view.translate
import org.camunda.bpm.application.ProcessApplication
import org.camunda.bpm.engine.ProcessEngines
import org.camunda.bpm.engine.RepositoryService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.camunda.bpm.spring.boot.starter.configuration.CamundaDatasourceConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.*


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@SpringBootApplication
@EnableProcessApplication
@Configuration
class PaymentRetrievalService {

    @Autowired
    private lateinit var repositoryService: RepositoryService

    @EventListener
    fun processPostDeploy(event: PostDeployEvent) {
        FlowDefinitions.all().forEach { flowDefinition ->
            val bpmn = transform(translate(flowDefinition))
            repositoryService
                .createDeployment()
                .addModelInstance("${flowDefinition.name}.bpmn", bpmn)
                .name(flowDefinition.name)
                .deploy()
        }
        CamundaBpmFlowExecutor.target(Message.from(RetrievePayment(reference = "anOrderId", accountId = "martin", payment = 5F))).map {
            CamundaBpmFlowExecutor.correlate(it)
        }

    }

    @Bean
    fun customJobHandler(): CamundaBpmFlowJobHandler {
        return CamundaBpmFlowJobHandler()
    }

}

fun main(args: Array<String>) {
    runApplication<PaymentRetrievalService>(*args) {
        PaymentRetrieval.init()
        CreditCardCharge.init()
    }
}

@RestController
class MessageController {

    @RequestMapping("/retrievePayment", method = [RequestMethod.POST])
    fun index(@RequestParam reference: String = "anOrderId", @RequestParam accountId: String = "anAccountId", @RequestParam payment: Float = 5F): String {
        val fact = RetrievePayment(reference, accountId, payment)
        return send(fact).toJson()
    }

    @RequestMapping("/confirmation", method = [RequestMethod.POST])
    fun index(@RequestParam reference: String = "anOrderId"): String {
        val fact = WaitForConfirmation(reference)
        return send(fact).toJson()
    }

    private fun send(fact: Fact): Message<*> {
        val message = Message.from(fact)
        CamundaBpmFlowExecutor.target(message).map {
            CamundaBpmFlowExecutor.correlate(it)
        }
        return message
    }

}