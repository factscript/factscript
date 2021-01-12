package org.factscript.language.execution.aws.translation.transition

import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder
import com.amazonaws.services.stepfunctions.builder.states.Transition
import org.factscript.language.definition.Node
import org.factscript.language.execution.aws.translation.context.TranslationContext

interface TransitionStrategy {
    fun nextTransition(translationContext: TranslationContext, node: Node): Transition.Builder
}

class SequentialTransitionStrategy : TransitionStrategy {
    override fun nextTransition(translationContext: TranslationContext, node: Node): Transition.Builder {
        val nextNode = node.forward
        return if(nextNode == null) StepFunctionBuilder.end() else StepFunctionBuilder.next(translationContext.namingStrategy.getName(nextNode))
    }
}

class ParallelTransitionStrategy(private val nextBranchingSibling: Node?) : TransitionStrategy {

    private val sequentialStrategy = SequentialTransitionStrategy()

    override fun nextTransition(translationContext: TranslationContext, node: Node): Transition.Builder {
        if(node.forward == nextBranchingSibling){
            return StepFunctionBuilder.end()
        }
        return sequentialStrategy.nextTransition(translationContext, node)
    }
}

class LastTransitionStrategy(private val lastChild: Node?, private val lastTransition : String) : TransitionStrategy {

    private val sequentialStrategy = SequentialTransitionStrategy()

    override fun nextTransition(translationContext: TranslationContext, node: Node): Transition.Builder {
        if(node == lastChild){
            return StepFunctionBuilder.next(lastTransition)
        }
        return sequentialStrategy.nextTransition(translationContext, node)
    }
}

class InclusiveTransitionStrategy(private val startNode: Node, private val nextBranchingSibling: Node?) : TransitionStrategy {

    private val sequentialStrategy = SequentialTransitionStrategy()

    override fun nextTransition(translationContext: TranslationContext, node: Node): Transition.Builder {
        if(node.forward == nextBranchingSibling){
            return StepFunctionBuilder.next(translationContext.namingStrategy.getName("while", startNode))
        }
        return sequentialStrategy.nextTransition(translationContext, node)
    }
}