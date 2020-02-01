package io.factdriven.flow.camunda

import io.factdriven.play.Player
import io.factdriven.play.camunda.CamundaFlowExecutionPlugin
import io.factdriven.play.camunda.CamundaProcessor
import io.factdriven.play.camunda.CamundaPublisher
import io.factdriven.play.camunda.CamundaRepository
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

