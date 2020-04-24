package io.factdriven.language.execution.aws.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.stepfunctions.model.SendTaskSuccessRequest
import io.factdriven.language.execution.aws.StateMachineService
import io.factdriven.language.execution.aws.example.function.PaymentRetrieval
import io.factdriven.language.execution.aws.example.function.RetrievePayment
import io.factdriven.language.execution.aws.translation.FlowTranslator
import io.factdriven.language.definition.*
import io.factdriven.execution.Fact
import io.factdriven.execution.Message
import io.factdriven.execution.newInstance
import io.factdriven.execution.type
import io.factdriven.language.impl.utils.compactJson
import io.factdriven.language.impl.utils.prettyJson
import java.util.stream.Collectors

abstract class FlowlangLambda : RequestHandler<Any, Any>{

    private val stateMachineService = StateMachineService()

    companion object {
        const val MESSAGE_KEY = "Messages"
        const val MESSAGE_KEY_PATH = "$MESSAGE_KEY.$"
        var context : Context? = null
        var input : Map<String, *>? = null
    }

    override fun handleRequest(i: Any?, context: Context?): Any {
        val input = i as Map<String, *>
        println(input.prettyJson)

        FlowlangLambda.context = context
        FlowlangLambda.input = input

        val definition = definition()
        val name = name(definition)
        val client = stateMachineService.createClient()

        val stateMachine = FlowTranslator.translate(definition)
        val json = stateMachine.toJson()
        println(json)

        if(isInitialization(input)){
            stateMachineService.createOrUpdateStateMachine(name, FlowTranslator.translate(definition))
            return "Done"
        }

        val token = input["TaskToken"] as String

        if(isMerge(input)){
            mergeList(token, input["MergeList"] as java.util.ArrayList<Map<String, Any>>)
            return "Merge Done"
        }

        // 32,768 characters max
        val messageList: MutableList<Message>
        if (input["History"] == null) {
            messageList = mutableListOf(createMessage(RetrievePayment("a", "b", 1f)))
        } else {
            println("daaa history $input[\"History\"]")
            messageList = toMessageList(input["History"] as ArrayList<String>)
            messageList.add(createMessage(RetrievePayment("a", "b", 1f)))
            val processInstance = messageList.newInstance(PaymentRetrieval::class)

            val node = definition.get(input["id"] as String)

            if(node is Branching){
                val sendTaskSuccessRequest = SendTaskSuccessRequest()
                        .withTaskToken(token)

                when (node.gateway) {
                    Gateway.Exclusive -> {
                        val result = evaluateConditions(processInstance, node)
                        sendTaskSuccessRequest.withOutput("\"$result\"")
                    }
                    Gateway.Inclusive -> {
                        val result = evaluateInclusiveConditions(processInstance, node)
                        sendTaskSuccessRequest.withOutput(result.compactJson)
                    }
                    else -> {
                        throw RuntimeException()
                    }
                }

                client.sendTaskSuccess(sendTaskSuccessRequest)
                return "Condition evaluated"
            }

            val fact = (node as Throwing).instance.invoke(processInstance)

            val message = createMessage(fact)
            messageList.add(message)

            println("${node.id} ${processInstance.prettyJson}")
        }

        val sendTaskSuccessRequest = SendTaskSuccessRequest()
                .withTaskToken(token)
                .withOutput(toStringList(messageList).prettyJson)

        client.sendTaskSuccess(sendTaskSuccessRequest)
        return "Message Done"
    }

    private fun mergeList(token: String, arrayList: java.util.ArrayList<Map<String, Any>>) {

        val messageSet = arrayList.stream().sequential()
                .flatMap { variables -> toMessageList(variables["Messages"] as java.util.ArrayList<String>).stream() }
                .collect(Collectors.toList()) //TODO unique items

        val sendTaskSuccessRequest = SendTaskSuccessRequest()
                .withTaskToken(token)
                .withOutput(FlowLangVariablesOut(messageSet.stream()
                        .map { message -> message.compactJson }
                        .collect(Collectors.toList())).compactJson)

        val client = stateMachineService.createClient()
        client.sendTaskSuccess(sendTaskSuccessRequest)
    }


    private fun evaluateConditions(processInstance: Any, branching: Branching) : String{
        for(conditionalExecution in branching.children){
            val conditional = conditionalExecution.children[0] as Conditional
            if(conditional.condition?.invoke(processInstance)!!){
                return io.factdriven.language.execution.aws.translation.toStateName(conditionalExecution)
            }
        }
        throw NoConditionMatchedException()
    }

    private fun evaluateInclusiveConditions(processInstance: Any, branching: Branching) : Map<String, Boolean>{
        val results = mutableMapOf<String, Boolean>()
        for((index, conditionalExecution) in branching.children.withIndex()){
            val conditional = conditionalExecution.children[0] as Conditional
            results["$index"] = conditional.condition?.invoke(processInstance)!!
        }
        return results
    }

    private fun toMessageList(arrayList: java.util.ArrayList<String>): MutableList<Message> {
        return arrayList.stream().map { s -> Message.fromJson(s) } .collect(Collectors.toList())
    }

    private fun toStringList(message: List<Message>): List<String> {
        return message.stream().map { m -> m.compactJson }.collect(Collectors.toList())
    }

    private fun createMessage(fact: Any): Message {
        return Message(fact.javaClass::class, Fact(fact))
    }

    private fun isInitialization(input: Any?) = input is Map<*, *> && input.isEmpty()

    private fun isMerge(input: Map<String, *>) = input["MergeList"] != null

    abstract fun definition() : Flow

    open fun name(flow: Flow) : String {
        return "${flow.entity.type}StateMachine"
    }
}