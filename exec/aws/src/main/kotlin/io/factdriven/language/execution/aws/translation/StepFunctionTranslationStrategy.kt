package io.factdriven.language.execution.aws.translation

import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.end
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.next
import com.amazonaws.services.stepfunctions.builder.states.Transition
import io.factdriven.language.definition.Node


abstract class StepFunctionTranslationStrategy(val flowTranslator: FlowTranslator) {
    abstract fun test(node: Node): Boolean
    abstract fun translate(translationContext: TranslationContext, node: Node)

    
}

open class TranslationContext(val transitionStrategy: TransitionStrategy, val stepFunctionBuilder: StepFunctionBuilder<*>) {
}

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
