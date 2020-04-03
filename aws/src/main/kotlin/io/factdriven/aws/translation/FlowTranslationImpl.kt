package io.factdriven.aws.translation

import com.amazonaws.services.stepfunctions.builder.StateMachine
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.end
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.next
import com.amazonaws.services.stepfunctions.builder.states.Transition
import io.factdriven.definition.api.Node

class ExecuteTranslationStrategy : FlowTranslationStrategy<Node>{

    override fun translate(stateMachineBuilder: StateMachine.Builder, node: Node) {
        stateMachineBuilder.state(node.id,
                StepFunctionBuilder.taskState()
                        .resource("arn:aws:lambda:REGION:ACCOUNT_ID:function:FUNCTION_NAME")
                        .transition(transitionTo(node))
        )
    }

    private fun transitionTo(node: Node): Transition.Builder? {
        if(node.isLast()){
            return end()
        }
        return next(node.next?.id)
    }
}