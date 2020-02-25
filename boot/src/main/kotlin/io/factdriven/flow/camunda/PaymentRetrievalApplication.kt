package io.factdriven.flow.camunda

import io.factdriven.execution.Player
import io.factdriven.execution.camunda.CamundaFlowExecutionPlugin
import io.factdriven.execution.camunda.CamundaProcessor
import io.factdriven.execution.camunda.CamundaPublisher
import io.factdriven.execution.camunda.CamundaRepository
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
        Player.register(CamundaProcessor())
        Player.register(CamundaPublisher())
        Player.register(CamundaRepository())
        PaymentRetrieval.init()
        CreditCardCharge.init()
    }
}

