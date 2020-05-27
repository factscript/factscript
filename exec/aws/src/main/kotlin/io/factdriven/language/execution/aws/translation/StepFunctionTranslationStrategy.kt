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

open class TranslationContext private constructor(val lambdaFunction: LambdaFunction,
                                                  val transitionStrategy: TransitionStrategy,
                                                  val stepFunctionBuilder: StepFunctionBuilder<*>,
                                                  val snsContext: SnsContext,
                                                  val namingStrategy: NamingStrategy = StatefulNamingStrategy()) {
    companion object {
        fun of(lambdaFunction: LambdaFunction,
               transitionStrategy: TransitionStrategy,
               stepFunctionBuilder: StepFunctionBuilder<*>,
               snsContext: SnsContext,
               namingStrategy: NamingStrategy = StatefulNamingStrategy()) : TranslationContext {
            return TranslationContext(lambdaFunction, transitionStrategy, stepFunctionBuilder, snsContext, namingStrategy)
        }
    }

    fun copyWith(lambdaFunction: LambdaFunction = this.lambdaFunction,
                 transitionStrategy: TransitionStrategy = this.transitionStrategy,
                 stepFunctionBuilder: StepFunctionBuilder<*> = this.stepFunctionBuilder,
                 snsContext: SnsContext = this.snsContext,
                 namingStrategy: NamingStrategy = this.namingStrategy): TranslationContext {
        return TranslationContext(lambdaFunction, transitionStrategy, stepFunctionBuilder, snsContext, namingStrategy)
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
    fun nextTransition(translationContext: TranslationContext, node: Node): Transition.Builder
}

class SequentialTransitionStrategy : TransitionStrategy {
    override fun nextTransition(translationContext: TranslationContext, node: Node): Transition.Builder {
        val nextNode = node.forward
        return if(nextNode == null) end() else next(translationContext.namingStrategy.getName(nextNode))
    }
}

class ParallelTransitionStrategy(private val nextBranchingSibling: Node?) : TransitionStrategy {

    private val sequentialStrategy = SequentialTransitionStrategy()

    override fun nextTransition(translationContext: TranslationContext, node: Node): Transition.Builder {
        if(node.forward == nextBranchingSibling){
            return end()
        }
        return sequentialStrategy.nextTransition(translationContext, node)
    }
}

class LoopTransitionStrategy(private val lastChild: Node?, private val lastTransition : String) : TransitionStrategy {

    private val sequentialStrategy = SequentialTransitionStrategy()

    override fun nextTransition(translationContext: TranslationContext, node: Node): Transition.Builder {
        if(node == lastChild){
            return next(lastTransition)
        }
        return sequentialStrategy.nextTransition(translationContext, node)
    }
}

class InclusiveTransitionStrategy(private val startNode: Node, private val nextBranchingSibling: Node?) : TransitionStrategy {

    private val sequentialStrategy = SequentialTransitionStrategy()

    override fun nextTransition(translationContext: TranslationContext, node: Node): Transition.Builder {
        if(node.forward == nextBranchingSibling){
            return next(translationContext.namingStrategy.getName("while", startNode))
        }
        return sequentialStrategy.nextTransition(translationContext, node)
    }
}

data class TranslationResult(val stateMachine: StateMachine, val translationContext: TranslationContext)

interface NamingStrategy {
    fun getName(node: Node) : String
    fun getName(prefix: String, node: Node) : String
}

class StatefulNamingStrategy : NamingStrategy {
    private val usedNames = HashMap<String, Int>()
    private val nodeLookup = HashMap<String, String>()

    override fun getName(prefix: String, node: Node): String {
        val lookupKey = getLookupKey(prefix, node)
        if(nodeLookup.containsKey(lookupKey)){
            return nodeLookup[lookupKey]!!
        }
        val collisionFreeName = getCollisionFreeName(toStateName(prefix, node))
        nodeLookup[getLookupKey(prefix, node)] = collisionFreeName
        return collisionFreeName
    }

    override fun getName(node: Node): String {
        val lookupKey = getLookupKey(null, node)
        if(nodeLookup.containsKey(lookupKey)){
            return nodeLookup[lookupKey]!!
        }
        val collisionFreeName = getCollisionFreeName(toStateName(node))
        nodeLookup[getLookupKey(null, node)] = collisionFreeName
        return collisionFreeName
    }

    private fun getLookupKey(prefix: String?, node: Node): String {
        if(prefix != null){
            return "$prefix-${node.id}"
        }
        return node.id
    }

    private fun getCollisionFreeName(name: String) : String{
        if(usedNames.containsKey(name)){
            val increment = usedNames[name]!!.plus(1)
            val collisionFreeName = "$name $increment"
            usedNames[name] = increment
            return collisionFreeName
        }
        usedNames[name] = 0
        return name
    }

    private fun toStateName(prefix: String?, node: Node) : String{
        if(prefix != null){
            if(node.description.isBlank()){
                return prefix
            }
            return "$prefix-${node.description}"
        }
        if(node.description.isBlank()){
            return node.id
        }
        return node.description
    }

    private fun toStateName(node: Node) : String{
        return toStateName(null, node)
    }

}