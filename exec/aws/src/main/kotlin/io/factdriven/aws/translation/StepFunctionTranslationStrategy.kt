package io.factdriven.aws.translation

import com.amazonaws.services.stepfunctions.builder.StateMachine
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.end
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.next
import com.amazonaws.services.stepfunctions.builder.states.Transition
import io.factdriven.definition.Branching
import io.factdriven.definition.Executing
import io.factdriven.definition.Node
import io.factdriven.language.Given
import kotlin.reflect.KClass


abstract class StepFunctionTranslationStrategy(val flowTranslator: FlowTranslator) {
    abstract fun test(node: Node): Boolean
    abstract fun translate(stateMachineBuilder: StateMachine.Builder, node: Node)

    protected fun transitionToNextOf(node: Node): Transition.Builder? {
        val nextNode = node.forward
        return if(nextNode == null) end() else next(name(nextNode))
    }
}