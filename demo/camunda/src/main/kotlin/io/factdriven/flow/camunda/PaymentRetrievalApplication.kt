package io.factdriven.flow.camunda

import io.factdriven.execution.Messages
import io.factdriven.language.execution.camunda.CamundaFlowExecutionPlugin
import io.factdriven.language.execution.camunda.CamundaMessageProcessor
import io.factdriven.language.execution.camunda.CamundaMessagePublisher
import io.factdriven.language.execution.camunda.CamundaMessageStore
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@SpringBootApplication @Configuration
@EnableProcessApplication
class PaymentRetrievalApplication {

    @Bean
    fun camundaFlowExecutionPlugin(): CamundaFlowExecutionPlugin {
        return CamundaFlowExecutionPlugin()
    }

}

fun main(args: Array<String>) {
    runApplication<PaymentRetrievalApplication>(*args) {
        Messages.register(CamundaMessageProcessor())
        Messages.register(CamundaMessagePublisher())
        Messages.register(CamundaMessageStore())
        PaymentRetrieval.init()
        CreditCardCharge.init()
        CustomerAccount.init()
    }
}

