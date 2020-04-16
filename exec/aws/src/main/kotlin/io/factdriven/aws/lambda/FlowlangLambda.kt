package io.factdriven.aws.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.stepfunctions.model.SendTaskSuccessRequest
import io.factdriven.aws.StateMachineService
import io.factdriven.aws.example.function.PaymentRetrieval
import io.factdriven.aws.example.function.RetrievePayment
import io.factdriven.aws.translation.FlowTranslator
import io.factdriven.definition.Flow
import io.factdriven.definition.Throwing
import io.factdriven.execution.Fact
import io.factdriven.execution.Message
import io.factdriven.execution.type
import io.factdriven.impl.utils.apply
import io.factdriven.impl.utils.compactJson
import io.factdriven.impl.utils.prettyJson
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

        // 32,768 characters max
        val messageList: MutableList<Message>
        if (input["History"] == null) {
            messageList = mutableListOf(createMessage(RetrievePayment("a", "b", 1f)))
        } else {
            messageList = toMessageList(input["History"] as ArrayList<String>)
            val processInstance = messageList.apply(PaymentRetrieval::class)

            val node = definition.get(input["id"] as String)
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

    abstract fun definition() : Flow

    open fun name(flow: Flow) : String {
        return "${flow.entity.type}StateMachine"
    }
}