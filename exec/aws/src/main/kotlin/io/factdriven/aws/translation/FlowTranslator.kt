package io.factdriven.aws.translation

import com.amazonaws.services.stepfunctions.builder.StateMachine
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder
import io.factdriven.definition.Flow
import io.factdriven.definition.Node
import java.util.stream.Collectors
import java.util.stream.Stream

open class FlowTranslator {
    companion object {
        fun translate(flow: Flow) : StateMachine {
            val stateMachineBuilder =  StepFunctionBuilder.stateMachine()
            FlowTranslator().traverseAndTranslateNodes(flow, stateMachineBuilder)
            return stateMachineBuilder.build()
        }
    }

    open fun traverseAndTranslateNodes(node: Node?, stateMachineBuilder: StateMachine.Builder) {
        var currentNode = node
        while (currentNode != null) {
            val strategy = determineTranslationStrategy(currentNode)
            strategy.translate(stateMachineBuilder, currentNode)

            currentNode = currentNode.nextSibling
        }
    }

    private fun determineTranslationStrategy(node: Node): StepFunctionTranslationStrategy {
        val matchingStrategies = Stream.of(*getTranslators())
                .filter { strategy -> strategy.test(node) }
                .collect(Collectors.toList())

        when (matchingStrategies.size) {
            1 -> return matchingStrategies[0]
            0 -> throw NoMatchingTranslatorFoundException()
            else -> throw MultipleMatchingStrategiesFoundExeption(matchingStrategies)
        }
    }

    open fun getTranslators() : Array<StepFunctionTranslationStrategy> {
        return arrayOf(
                GivenTranslator(this),
                FlowTranslationStrategy(this),
                XOrTranslationStrategy(this),
                ExecuteTranslationStrategy(this)
        )
    }
}