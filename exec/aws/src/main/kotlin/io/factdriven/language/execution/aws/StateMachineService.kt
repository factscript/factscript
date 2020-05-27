package io.factdriven.language.execution.aws

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.stepfunctions.AWSStepFunctions
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder
import com.amazonaws.services.stepfunctions.builder.StateMachine
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder
import com.amazonaws.services.stepfunctions.builder.StepFunctionBuilder.next
import com.amazonaws.services.stepfunctions.builder.conditions.NumericEqualsCondition
import com.amazonaws.services.stepfunctions.builder.states.Choice
import com.amazonaws.services.stepfunctions.builder.states.ChoiceState
import com.amazonaws.services.stepfunctions.model.*
import io.factdriven.language.execution.aws.lambda.LambdaInitializationContext
import io.factdriven.language.impl.utils.compactJson

class StateMachineService {
    fun createDummyStateMachineDefinition() : StateMachine {
        return StepFunctionBuilder.stateMachine()
                .startAt("a")
                .state("a", ChoiceState.builder().choices(
                        Choice.builder().condition(NumericEqualsCondition.builder().expectedValue(0L).variable("$.output.my"))
                                .transition(next("Example")),
                        Choice.builder().condition(NumericEqualsCondition.builder().expectedValue(1L).variable("$.output.my"))
                                .transition(next("Example2"))
                ))
                .state("Example",
                        StepFunctionBuilder.taskState()
                                .resource("arn:aws:lambda:REGION:ACCOUNT_ID:function:FUNCTION_NAME")
                                .transition(StepFunctionBuilder.end())
                )
                .state("Example2",
                        StepFunctionBuilder.taskState()
                                .resource("arn:aws:lambda:REGION:ACCOUNT_ID:function:FUNCTION_NAME")
                                .transition(StepFunctionBuilder.end())
                )
                .build()
    }

    fun createOrUpdateStateMachine(lambdaInitializationContext: LambdaInitializationContext, stateMachine: StateMachine) : String? {
        val client = createClient()
        val updateArn: String?
        val stepFunctionRoleArn = System.getenv("STEP_FUNCTION_ROLE_ARN")
        println(stepFunctionRoleArn)

        try {
            val resultCreate = client.createStateMachine(CreateStateMachineRequest()
                    .withName(lambdaInitializationContext.stateMachine.name)
                    .withRoleArn(stepFunctionRoleArn)
                    .withDefinition(stateMachine))

            println("creation result: $resultCreate")
            return resultCreate.stateMachineArn
        } catch (e: StateMachineAlreadyExistsException){
            e.printStackTrace()
            updateArn = Regex("arn.*(?=\')").find(e.message.orEmpty())?.value
        }

        if(updateArn != null) {
            println("updateArn is $updateArn")
            val result = client.updateStateMachine(UpdateStateMachineRequest()
                    .withStateMachineArn(updateArn)
                    .withRoleArn(stepFunctionRoleArn)
                    .withDefinition(stateMachine.toPrettyJson()))

            println("update result is $result")
        }

        return updateArn
    }

    fun getStateMachineArn(name: String) : String{
        val listStateMachines = createClient().listStateMachines(ListStateMachinesRequest())
        val stateMachine = listStateMachines.stateMachines.first { s -> s.name == name }
        return stateMachine.stateMachineArn
    }

    fun createClient(): AWSStepFunctions {
        val client = AWSStepFunctionsClientBuilder.standard()
                .withRegion("eu-central-1")
                .withCredentials(DefaultAWSCredentialsProviderChain())
                .build()
        return client
    }

    fun execute(arn: String?, input: Any) {
        val client = createClient()
        val startExecutionRequest: StartExecutionRequest = StartExecutionRequest().withStateMachineArn(arn).withInput(input.compactJson)
        client.startExecution(startExecutionRequest)
    }

    fun list(){
        val client = createClient()
        val listStateMachines = client.listStateMachines(ListStateMachinesRequest())
        for (stateMachine in listStateMachines.stateMachines) {
            val describeStateMachine = client.describeStateMachine(DescribeStateMachineRequest())
            val describeStateMachineForExecution = client.describeStateMachineForExecution(DescribeStateMachineForExecutionRequest())
            val describeExecution = client.describeExecution(DescribeExecutionRequest())
            val describeActivity = client.describeActivity(DescribeActivityRequest())

        }
    }
}