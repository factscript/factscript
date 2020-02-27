package io.factdriven.aws

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder
import com.amazonaws.services.stepfunctions.builder.StateMachine
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder
import com.amazonaws.services.stepfunctions.model.CreateStateMachineRequest
import com.amazonaws.services.stepfunctions.model.UpdateStateMachineRequest

class StateMachineService {
    fun createDummyStateMachineDefinition() : StateMachine {
        return StepFunctionBuilder.stateMachine()
                .startAt("Example")
                .state("Example",
                        StepFunctionBuilder.taskState()
                                .resource("arn:aws:lambda:REGION:ACCOUNT_ID:function:FUNCTION_NAME")
                                .transition(StepFunctionBuilder.end())
                )
                .build()
    }

    fun createOrUpdateStateMachine(name: String, stateMachine: StateMachine) : String? {
        val client = AWSStepFunctionsClientBuilder.standard()
                .withRegion("eu-central-1")
                .withCredentials(DefaultAWSCredentialsProviderChain())
                .build()

        val updateArn: String?
        val stepFunctionRoleArn = System.getenv("STEP_FUNCTION_ROLE_ARN")

        try {
            val resultCreate = client.createStateMachine(CreateStateMachineRequest()
                    .withName(name)
                    .withRoleArn(stepFunctionRoleArn)
                    .withDefinition(stateMachine))

            println(resultCreate.toString())
            return resultCreate.stateMachineArn
        } catch (e: Exception){
            e.printStackTrace()
            //TODO: parse specific "already exists" message
            updateArn = Regex("arn.*(?=\')").find(e.message.orEmpty())?.value
        }

        if(updateArn != null) {
            val result = client.updateStateMachine(UpdateStateMachineRequest()
                    .withStateMachineArn(updateArn)
                    .withRoleArn(stepFunctionRoleArn)
                    .withDefinition(stateMachine.toPrettyJson()))

            println(result.toString())
        }

        return updateArn
    }
}