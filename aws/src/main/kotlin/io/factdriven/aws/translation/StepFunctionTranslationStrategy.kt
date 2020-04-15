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

    protected fun determineNextNode(node: Node) : Node? {
        val nextNode = getNextNode(node)
        if(nextNode != null){
            return nextNode
        }

        if(node.parent is Executing && node.parent?.parent == null){
            return null
        }

        val branching = getBranchingParent(node)

        if(branching != null){

            if(branching.next == null){
                return determineNextNode(branching)
            }

            return branching.next
        }

        return null
    }

    protected fun getNextNode(node: Node) : Node? {
        val skipList = listOf<KClass<*>>(Given::class)

        if(node.next == null){
            return null
        }

        if(skipList.contains(node.next!!::class)){
            return getNextNode(node.next!!)
        }
        return node.next
    }

    protected fun getBranchingParent(node: Node) : Branching?{
        if(node.parent is Branching)
            return node.parent as Branching
        else if(node.parent == null){
            return null
        } else {
            return getBranchingParent(node.parent!!)
        }

    }

    protected fun transitionToNextOf(node: Node): Transition.Builder? {
        val nextNode = determineNextNode(node)
        return if(nextNode == null) end() else next(name(nextNode))
    }
}