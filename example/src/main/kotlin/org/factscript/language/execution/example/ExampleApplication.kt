package org.factscript.language.execution.example

import org.factscript.execution.Messages
import org.factscript.language.*
import org.factscript.language.execution.cam.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication @Configuration
@EnableProcessApplication
class ExampleApplication {

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

    runApplication<ExampleApplication>(*args) {

        Messages.register(EngineMessageProcessor())
        Messages.register(EngineMessagePublisher())
        Messages.register(EngineMessageStore())

        Flows.activate(
                CreditCard::class
        )

    }

}