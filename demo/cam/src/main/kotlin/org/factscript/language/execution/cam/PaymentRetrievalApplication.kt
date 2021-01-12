package org.factscript.language.execution.cam

import org.factscript.execution.Messages
import org.factscript.language.*
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
    fun camundaFlowExecutionPlugin(): FactscriptLanguagePlugin {
        return FactscriptLanguagePlugin()
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

        Flows.activate(
            org.factscript.language.execution.cam.Fulfillment::class, Shipment::class, org.factscript.language.execution.cam.Inventory1::class, org.factscript.language.execution.cam.Inventory2::class,
            org.factscript.language.execution.cam.Payment::class, org.factscript.language.execution.cam.Account1::class, org.factscript.language.execution.cam.Account2::class, org.factscript.language.execution.cam.CreditCard::class
        )

    }

}

