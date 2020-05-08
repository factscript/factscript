package io.factdriven.language.execution.aws.translation

import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.end
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.next
import com.amazonaws.services.stepfunctions.builder.states.Transition
import io.factdriven.execution.type
import io.factdriven.language.definition.Node
import kotlin.reflect.KClass


abstract class StepFunctionTranslationStrategy(val flowTranslator: FlowTranslator) {
    abstract fun test(node: Node): Boolean
    abstract fun translate(translationContext: TranslationContext, node: Node)

    
}

open class TranslationContext private constructor(val lambdaFunction: LambdaFunction, val transitionStrategy: TransitionStrategy, val stepFunctionBuilder: StepFunctionBuilder<*>) {
    companion object {
        fun of(lambdaFunction: LambdaFunction, transitionStrategy: TransitionStrategy, stepFunctionBuilder: StepFunctionBuilder<*>) : TranslationContext {
            return TranslationContext(lambdaFunction, transitionStrategy, stepFunctionBuilder)
        }
    }

    fun copyWith(lambdaFunction: LambdaFunction = this.lambdaFunction, transitionStrategy: TransitionStrategy = this.transitionStrategy, stepFunctionBuilder: StepFunctionBuilder<*> = this.stepFunctionBuilder): TranslationContext {
        return TranslationContext(lambdaFunction, transitionStrategy, stepFunctionBuilder)
    }

}

data class LambdaFunction(val name: String, val resource: String = "arn:aws:states:::lambda:invoke.waitForTaskToken")

interface TransitionStrategy {
    fun nextTransition(node: Node): Transition.Builder
}

class SequentialTransitionStrategy : TransitionStrategy {
    override fun nextTransition(node: Node): Transition.Builder {
        val nextNode = node.forward
        return if(nextNode == null) end() else next(toStateName(nextNode))
    }
}

class ParallelTransitionStrategy(private val nextBranchingSibling: Node?) : TransitionStrategy {

    private val sequentialStrategy = SequentialTransitionStrategy()

    override fun nextTransition(node: Node): Transition.Builder {
        if(node.forward == nextBranchingSibling){
            return end()
        }
        return sequentialStrategy.nextTransition(node)
    }
}

class LoopTransitionStrategy(private val lastChild: Node?, private val lastTransition : String) : TransitionStrategy {

    private val sequentialStrategy = SequentialTransitionStrategy()

    override fun nextTransition(node: Node): Transition.Builder {
        if(node == lastChild){
            return next(lastTransition)
        }
        return sequentialStrategy.nextTransition(node)
    }
}

class InclusiveTransitionStrategy(private val startNode: Node, private val nextBranchingSibling: Node?) : TransitionStrategy {

    private val sequentialStrategy = SequentialTransitionStrategy()

    override fun nextTransition(node: Node): Transition.Builder {
        if(node.forward == nextBranchingSibling){
            return next(toStateName("while", startNode))
        }
        return sequentialStrategy.nextTransition(node)
    }
}
