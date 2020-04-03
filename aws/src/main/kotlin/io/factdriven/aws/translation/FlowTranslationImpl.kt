package io.factdriven.aws.translation

import com.amazonaws.services.stepfunctions.builder.StateMachine
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.end
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.next
import com.amazonaws.services.stepfunctions.builder.states.Transition
import io.factdriven.definition.api.Node
import io.factdriven.traverse.NodeTraverse

class ExecuteTranslationStrategy : FlowTranslationStrategy<Node>{

    override fun translate(stateMachineBuilder: StateMachine.Builder, nodeTraverse: NodeTraverse) {
        stateMachineBuilder.state(nodeTraverse.name(),
                StepFunctionBuilder.taskState()
                        .resource("arn:aws:lambda:REGION:ACCOUNT_ID:function:FUNCTION_NAME")
                        .transition(transitionTo(nodeTraverse))
        )
    }

    private fun transitionTo(nodeTraverse: NodeTraverse): Transition.Builder? {
        if(nodeTraverse.isEnd()){
            return end()
        }
        return next(nodeTraverse.nextTraverse?.name())
    }
}