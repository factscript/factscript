package io.factdriven.language.execution.aws.translation

import com.amazonaws.services.stepfunctions.builder.StateMachine
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.end
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.next
import com.amazonaws.services.stepfunctions.builder.states.Transition
import io.factdriven.language.definition.Consuming
import io.factdriven.language.definition.Node
import io.factdriven.language.definition.Throwing


abstract class StepFunctionTranslationStrategy(val flowTranslator: FlowTranslator) {
    abstract fun test(node: Node): Boolean
    abstract fun translate(translationContext: TranslationContext, node: Node)

    
}

open class TranslationContext private constructor(val lambdaFunction: LambdaFunction, val transitionStrategy: TransitionStrategy, val stepFunctionBuilder: StepFunctionBuilder<*>, val snsContext: SnsContext) {
    companion object {
        fun of(lambdaFunction: LambdaFunction, transitionStrategy: TransitionStrategy, stepFunctionBuilder: StepFunctionBuilder<*>, snsContext: SnsContext) : TranslationContext {
            return TranslationContext(lambdaFunction, transitionStrategy, stepFunctionBuilder, snsContext)
        }
    }

    fun copyWith(lambdaFunction: LambdaFunction = this.lambdaFunction, transitionStrategy: TransitionStrategy = this.transitionStrategy, stepFunctionBuilder: StepFunctionBuilder<*> = this.stepFunctionBuilder, snsContext: SnsContext = this.snsContext): TranslationContext {
        return TranslationContext(lambdaFunction, transitionStrategy, stepFunctionBuilder, snsContext)
    }

}

data class LambdaFunction(val name: String, val resource: String = "arn:aws:states:::lambda:invoke.waitForTaskToken")

class SnsContext(val namespace: String, val topics : MutableList<Topic> = mutableListOf(), val resource : String = "arn:aws:states:::sns:publish"){
    class Topic (val name: String, val type : Type) {
        enum class Type {
            Throwing, Consuming
        }
    }

    fun getTopicArn(name : String) : String{
        return "$namespace$name"
    }

    fun getTopicArn(node: Node) : String {
        return if(node is Consuming){
            getConsumingArnTopic(node)
        } else {
            getThrowingArnTopic(node as Throwing)
        }
    }

    private fun getThrowingArnTopic(throwing: Throwing) : String {
        return getTopicArn(throwing.throwing.simpleName!!)
    }

    private fun getConsumingArnTopic(consuming: Consuming) : String {
        return getTopicArn(consuming.consuming.simpleName!!)
    }

    fun addTopic(node : Node){
        if(node is Throwing){
            topics.add(Topic(node.throwing.simpleName!!, Topic.Type.Throwing))
        } else if (node is Consuming) {
            topics.add(Topic(node.consuming.simpleName!!, Topic.Type.Consuming))
        }
    }

    fun getSubscriptionTopicArns(): List<String> {
        return topics
                .filter { topic -> topic.type == Topic.Type.Consuming }
                .map { topic -> getTopicArn(topic.name) }
                .toList()
    }

    fun getPublishTopicArns(): List<String> {
        return topics
                .filter { topic -> topic.type == Topic.Type.Throwing }
                .map { topic -> getTopicArn(topic.name) }
                .toList()
    }

    fun getAllTopicArns() : List<String> {
        return topics
                .map { topic -> getTopicArn(topic.name) }
                .toList()
    }

    fun getAllTopicNames() : List<String> {
        return topics
                .map { topic -> topic.name}
                .toList()
    }
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

data class TranslationResult(val stateMachine: StateMachine, val translationContext: TranslationContext)