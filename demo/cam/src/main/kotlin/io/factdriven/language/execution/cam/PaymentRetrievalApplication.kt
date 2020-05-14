package io.factdriven.language.execution.cam

import io.factdriven.execution.Messages
import io.factdriven.language.*
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
    fun camundaFlowExecutionPlugin(): FactDrivenLanguagePlugin {
        return FactDrivenLanguagePlugin()
    }

    @Bean
    fun condition(): EngineCondition {
        return EngineCondition()
    }

}

fun main(args: Array<String>) {

    runApplication<PaymentRetrievalApplication>(*args) {

        Messages.register(EngineMessageProcessor())
        Messages.register(EngineMessagePublisher())
        Messages.register(EngineMessageStore())

        Flows.activate(Fulfillment::class, Shipment::class, Inventory1::class, Inventory2::class,
            Payment::class, Account1::class, Account2::class, CreditCard::class)

    }

}

