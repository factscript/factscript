package io.factdriven.aws.translation

import com.amazonaws.services.stepfunctions.builder.StateMachine
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.end
import io.factdriven.definition.Node
import io.factdriven.definition.typeName

class ExecuteTranslationStrategy : FlowTranslationStrategy<Node>{

    override fun translate(stateMachineBuilder: StateMachine.Builder, node: Node) {
        stateMachineBuilder.state(node.typeName,
                StepFunctionBuilder.taskState()
                        .resource("arn:aws:lambda:REGION:ACCOUNT_ID:function:FUNCTION_NAME")
                        .transition(end()
                        )
        )
    }
}