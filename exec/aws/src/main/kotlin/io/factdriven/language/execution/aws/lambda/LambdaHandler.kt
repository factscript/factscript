package io.factdriven.language.execution.aws.lambda

import com.amazonaws.services.stepfunctions.model.SendTaskSuccessRequest
import io.factdriven.execution.Fact
import io.factdriven.execution.Message
import io.factdriven.language.definition.*
import io.factdriven.language.execution.aws.StateMachineService
import io.factdriven.language.execution.aws.translation.FlowTranslator
import io.factdriven.language.execution.aws.translation.toStateName
import io.factdriven.language.impl.utils.compactJson
import io.factdriven.language.impl.utils.prettyJson
import java.lang.IllegalArgumentException
import java.util.stream.Collectors

data class HandlerResult(val output: String){
    companion object{
        val OK = HandlerResult("Ok")
    }

    override fun toString(): String {
        return output
    }
}

abstract class LambdaHandler {
    abstract fun test(lambdaContext: LambdaContext) : Boolean
    abstract fun handle(lambdaContext: LambdaContext) : HandlerResult

    protected fun toStringList(message: List<Message>): List<String> {
        return message.stream().map { m -> m.compactJson }.collect(Collectors.toList())
    }

    protected fun createMessage(fact: Any): Message {
        return Message(fact.javaClass::class, Fact(fact))
    }
}

class InitializationHandler : LambdaHandler(){
    private val stateMachineService = StateMachineService()

    override fun test(lambdaContext: LambdaContext): Boolean {
        return lambdaContext.state == LambdaContext.State.INITIALIZATION
    }

    override fun handle(lambdaContext: LambdaContext) : HandlerResult {
        val context = lambdaContext as LambdaInitializationContext
        stateMachineService.createOrUpdateStateMachine(context,
                FlowTranslator.translate(context.definition))
        return HandlerResult.OK
    }
}

class MergeHandler : LambdaHandler(){
    override fun test(lambdaContext: LambdaContext): Boolean {
        return lambdaContext.state == LambdaContext.State.EXECUTION && isMerge(lambdaContext as ProcessContext)
    }

    private fun isMerge(processContext: ProcessContext) = processContext.input["MergeList"] != null

    override fun handle(lambdaContext: LambdaContext) : HandlerResult{
        val processContext = lambdaContext as ProcessContext
        val input = processContext.input["MergeList"] as ArrayList<Map<String, *>>
        val messageSet = input.stream().sequential()
                .flatMap { variables -> processContext.toMessageList(variables["Messages"] as java.util.ArrayList<String>).stream() }
                .collect(Collectors.toList()) //TODO unique items

        val compactJson = FlowLangVariablesOut(messageSet.stream()
                .map { message -> message.compactJson }
                .collect(Collectors.toList())).compactJson
        val client = StateMachineService().createClient()

        val sendTaskSuccessRequest = SendTaskSuccessRequest()
                .withTaskToken(processContext.token)
                .withOutput(compactJson)

        client.sendTaskSuccess(sendTaskSuccessRequest)

        return HandlerResult(compactJson)
    }
}

abstract class NodeHandler : LambdaHandler (){
    private val stateMachineService = StateMachineService()

    final override fun test(lambdaContext: LambdaContext): Boolean {
        return lambdaContext is ProcessContext && test(lambdaContext)
    }

    final override fun handle(lambdaContext: LambdaContext) : HandlerResult{
        if(lambdaContext !is ProcessContext) {
            throw IllegalArgumentException("Need a ProcessContext")
        }
        val result = handle(lambdaContext)
        handleSuccess(lambdaContext, result)
        return HandlerResult.OK
    }

    abstract fun test(processContext: ProcessContext) : Boolean
    abstract fun handle(processContext: ProcessContext) : HandlerResult

    open fun handleSuccess(processContext: ProcessContext, result: HandlerResult){
        val client = stateMachineService.createClient()
        val sendTaskSuccessRequest = SendTaskSuccessRequest()
                .withTaskToken(processContext.token)
                .withOutput(result.output)
        client.sendTaskSuccess(sendTaskSuccessRequest)
    }
}

class PromisingHandler : NodeHandler(){
    override fun test(processContext: ProcessContext): Boolean {
        return processContext.node is Promising
    }

    override fun handle(processContext: ProcessContext): HandlerResult {
        val messageList = processContext.messageList
        return HandlerResult(toStringList(messageList).prettyJson)
    }
}

class ExecutionHandler : NodeHandler(){
    override fun test(processContext: ProcessContext): Boolean {
        return processContext.node !is Branching
    }

    override fun handle(processContext: ProcessContext) : HandlerResult {
        val fact = (processContext.node as Throwing).instance.invoke(processContext.processInstance)
        val messageList = processContext.messageList
        val message = createMessage(fact)
        messageList.add(message)
        return HandlerResult(toStringList(messageList).prettyJson)
    }
}

class InclusiveHandler : NodeHandler() {
    override fun test(processContext: ProcessContext): Boolean {
        val node = processContext.node
        return node is Branching && node.gateway == Gateway.Inclusive
    }

    override fun handle(processContext: ProcessContext) : HandlerResult {
        val result = evaluateInclusiveConditions(processContext.processInstance, processContext.node as Branching)
        return HandlerResult(result.compactJson)
    }

    private fun evaluateInclusiveConditions(processInstance: Any, branching: Branching) : Map<String, Boolean>{
        val results = mutableMapOf<String, Boolean>()
        for((index, conditionalExecution) in branching.children.withIndex()){
            val conditional = conditionalExecution.children[0] as Conditional
            results["$index"] = conditional.condition?.invoke(processInstance)!!
        }
        return results
    }

}

class ExclusiveHandler : NodeHandler() {
    override fun test(processContext: ProcessContext): Boolean {
        val node = processContext.node
        return node is Branching && node.gateway == Gateway.Exclusive
    }

    override fun handle(processContext: ProcessContext) : HandlerResult {
        val result = evaluateConditions(processContext.processInstance, processContext.node as Branching)
        return HandlerResult("\"$result\"")
    }

    private fun evaluateConditions(processInstance: Any, branching: Branching) : String{
        for(conditionalExecution in branching.children){
            val conditional = conditionalExecution.children[0] as Conditional
            if(conditional.condition?.invoke(processInstance)!!){
                return toStateName(conditionalExecution)
            }
        }
        throw NoConditionMatchedException()
    }
}

