package io.factdriven.aws.translation

import com.amazonaws.services.stepfunctions.builder.StateMachine
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder
import io.factdriven.definition.Definition
import io.factdriven.definition.Node
import io.factdriven.definition.typeName

class FlowTranslator {

    companion object {
        fun translate(definition: Definition) : StateMachine {
            val stateMachineBuilder =  StepFunctionBuilder.stateMachine()
            val flowTranslator = FlowTranslator()

            stateMachineBuilder.startAt(definition.children.first().typeName)

            for (child in definition.children) {
                flowTranslator.translate(stateMachineBuilder, child)
            }

            return stateMachineBuilder.build()
        }
    }

    private fun translate(stateMachineBuilder: StateMachine.Builder, node: Node){
        val strategy = determineTranslationStrategy(node)
        strategy.translate(stateMachineBuilder, node)

        for (child in node.children) {
            translate(stateMachineBuilder, node)
        }

    }

    private fun determineTranslationStrategy(node: Node): FlowTranslationStrategy<Node> {
        return ExecuteTranslationStrategy()
    }
}