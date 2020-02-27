package io.factdriven.aws.example.function

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import io.factdriven.aws.StateMachineService

class MyFunctionKt : RequestHandler<Any, String> {

    private val stateMachineService = StateMachineService()

    override fun handleRequest(input: Any?, context: Context?): String {
        val name = "${javaClass.simpleName}StateMachine"
        val dummyDefinition = stateMachineService.createDummyStateMachineDefinition()
        val arn = stateMachineService.createOrUpdateStateMachine(name, dummyDefinition)

        return "Hello Kotlin Lambda Function! StateMachine arn is $arn"
    }
}