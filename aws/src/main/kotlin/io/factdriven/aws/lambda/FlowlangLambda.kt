package io.factdriven.aws.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.stepfunctions.builder.StateMachine
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.*
import com.amazonaws.services.stepfunctions.builder.states.Transition
import io.factdriven.aws.StateMachineService
import io.factdriven.definition.Definition
import io.factdriven.definition.Node
import io.factdriven.definition.typeName

abstract class FlowlangLambda : RequestHandler<Any, String>{

    private val stateMachineService = StateMachineService()

    override fun handleRequest(input: Any?, context: Context?): String? {
        val definition = definition()
        val name = name(definition)
        return stateMachineService.createOrUpdateStateMachine(name, translate(definition))
    }

    abstract fun definition() : Definition

    open fun name(definition: Definition) : String {
        return "${definition.typeName}StateMachine"
    }

    private fun translate(definition: Definition): StateMachine {
        val stateMachineBuilder = stateMachine()

        var isStart = true
        for (child in definition.children){

            if(isStart) {
                stateMachineBuilder.startAt(child.typeName)
                isStart = false
            }

            stateMachineBuilder.state(child.typeName,
                    taskState()
                            .resource("arn:aws:lambda:REGION:ACCOUNT_ID:function:FUNCTION_NAME")
                            .transition(nextTransition(definition.children, child)
                    )
            )
        }

        return stateMachineBuilder.build()
    }

    private fun nextTransition(children: List<Node>, currentNode: Node) : Transition.Builder{
        var childFound = false

        for(child in children){
            if(childFound){
                return next(child.typeName)
            }
            if(child == currentNode){
                childFound = true
            }
        }

        return end()
    }
}