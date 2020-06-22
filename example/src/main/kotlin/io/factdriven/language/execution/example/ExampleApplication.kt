package io.factdriven.language.execution.example

import io.factdriven.execution.Messages
import io.factdriven.language.*
import io.factdriven.language.execution.cam.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication @Configuration
@EnableProcessApplication
class ExampleApplication {

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

    runApplication<ExampleApplication>(*args) {

        Messages.register(EngineMessageProcessor())
        Messages.register(EngineMessagePublisher())
        Messages.register(EngineMessageStore())

        Flows.activate(
                CreditCard::class
        )

    }

}