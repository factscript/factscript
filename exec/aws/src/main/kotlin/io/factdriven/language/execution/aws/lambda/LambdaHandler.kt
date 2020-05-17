package io.factdriven.language.execution.aws.lambda

import com.amazonaws.services.sns.model.CreateTopicRequest
import com.amazonaws.services.sns.model.SubscribeRequest
import com.amazonaws.services.stepfunctions.model.SendTaskSuccessRequest
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.factdriven.execution.Fact
import io.factdriven.execution.Message
import io.factdriven.language.Execute
import io.factdriven.language.Loop
import io.factdriven.language.definition.*
import io.factdriven.language.execution.aws.LambdaService
import io.factdriven.language.execution.aws.SnsService
import io.factdriven.language.execution.aws.StateMachineService
import io.factdriven.language.execution.aws.translation.FlowTranslator
import io.factdriven.language.execution.aws.translation.LambdaFunction
import io.factdriven.language.execution.aws.translation.toStateName
import io.factdriven.language.impl.utils.compactJson
import java.lang.IllegalArgumentException
import java.util.stream.Collectors

data class HandlerResult(val output: Any){
    companion object{
        val OK = HandlerResult("Ok")
    }

    override fun toString(): String {
        return if (output is String){
            output
        } else {
            output.compactJson
        }
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
    private val snsService = SnsService()
    private val lambdaService = LambdaService()

    override fun test(lambdaContext: LambdaContext): Boolean {
        return lambdaContext.state == LambdaContext.State.INITIALIZATION
    }

    override fun handle(lambdaContext: LambdaContext) : HandlerResult {
        val context = lambdaContext as LambdaInitializationContext
        val translationResult = FlowTranslator.translate(context.definition, LambdaFunction(lambdaContext.context.functionName))
        stateMachineService.createOrUpdateStateMachine(context,
                translationResult.stateMachine)

        val snsContext = translationResult.translationContext.snsContext
        snsService.createTopics(snsContext.getAllTopicNames())
        snsService.subscribeTopics(lambdaContext.context.invokedFunctionArn, snsContext.getSubscriptionTopicArns())

        lambdaService.updateTriggers(lambdaContext.context.invokedFunctionArn, snsContext.getSubscriptionTopicArns())

        return HandlerResult.OK
    }
}
abstract class EventHandler : LambdaHandler(){

    final override fun test(lambdaContext: LambdaContext): Boolean {
        return lambdaContext is EventContext && testEvent(lambdaContext)
    }

    final override fun handle(lambdaContext: LambdaContext): HandlerResult {
        return handleEvent(lambdaContext as EventContext)
    }

    abstract fun testEvent(eventContext: EventContext) : Boolean
    abstract fun handleEvent(eventContext: EventContext) : HandlerResult


}
class OnHandler : EventHandler() {

    private val stateMachineService = StateMachineService()

    override fun testEvent(eventContext: EventContext): Boolean {
        return eventContext.node is Promising
    }

    override fun handleEvent(eventContext: EventContext): HandlerResult {
        val stateMachineArn = stateMachineService.getStateMachineArn(eventContext.stateMachine.name)
        stateMachineService.execute(stateMachineArn, StepFunctionStarter(arrayListOf(createMessage(eventContext.event).compactJson)))

        return HandlerResult.OK
    }

    data class StepFunctionStarter(@JsonProperty("Messages") val messages : ArrayList<String>)
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
        println("invoking handler " + this.javaClass)
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
                .withOutput(result.toString())
        client.sendTaskSuccess(sendTaskSuccessRequest)
    }
}

class PromisingHandler : NodeHandler(){
    override fun test(processContext: ProcessContext): Boolean {
        return processContext.node is Promising
    }

    override fun handle(processContext: ProcessContext): HandlerResult {
        val messageList = processContext.messageList
        return HandlerResult(toStringList(messageList))
    }
}

class ExecutionHandler : NodeHandler(){
    override fun test(processContext: ProcessContext): Boolean {
        return processContext.node is Execute<*>
    }

    override fun handle(processContext: ProcessContext) : HandlerResult {
        val fact = (processContext.node as Throwing).factory.invoke(processContext.processInstance)
        val messageList = processContext.messageList
        val message = createMessage(fact)
        messageList.add(message)
        return HandlerResult(toStringList(messageList))
    }
}

class ThrowingHandler : NodeHandler(){
    private val snsService = SnsService()

    override fun test(processContext: ProcessContext): Boolean {
        return processContext.node is Throwing && processContext.node !is Execute<*>
    }

    override fun handle(processContext: ProcessContext) : HandlerResult {
        val fact = (processContext.node as Throwing).factory.invoke(processContext.processInstance)
        val messageList = processContext.messageList
        val message = createMessage(fact)
        messageList.add(message)

        publishEvent(processContext, fact)

        return HandlerResult(toStringList(messageList))
    }

    private fun publishEvent(processContext: ProcessContext, fact: Any){
        val (_, translationContext) = FlowTranslator.translate(processContext.definition, LambdaFunction(processContext.context.functionName))
        val topicArn = translationContext.snsContext.getTopicArn(processContext.node!!)
        snsService.publishMessage(topicArn = topicArn, subject = fact::class.qualifiedName!!, message = fact.compactJson)
    }
}

class InclusiveHandler : NodeHandler() {
    override fun test(processContext: ProcessContext): Boolean {
        val node = processContext.node
        return node is Branching && node.fork == Junction.Some
    }

    override fun handle(processContext: ProcessContext) : HandlerResult {
        val result = evaluateInclusiveConditions(processContext, processContext.node as Branching)
        return HandlerResult(result)
    }

    private fun evaluateInclusiveConditions(processContext: ProcessContext, branching: Branching) : InclusiveContext{
        val inclusiveContext : InclusiveContext?
        if(processContext.input["InclusiveContext"] != null){
           inclusiveContext = ObjectMapper().convertValue(processContext.input["InclusiveContext"], InclusiveContext::class.java)
        } else {
            return buildInclusiveConditions(processContext, branching)
        }
        for((index, _) in branching.children.withIndex()){
            val isCurrentIteration = index >= inclusiveContext.next+1 && index < branching.children.size - 1
            inclusiveContext.conditions["$index"] = isCurrentIteration && inclusiveContext.conditions["$index"]!!
        }
        return if(inclusiveContext.conditions.values.count { value -> value } > 1) {
            InclusiveContext(inclusiveContext.conditions, next = inclusiveContext.conditions.values.indexOf(true), counter = inclusiveContext.counter+1)
        } else {
            InclusiveContext(inclusiveContext.conditions, next = -1, counter = inclusiveContext.counter+1)
        }
    }

    private fun buildInclusiveConditions(processContext: ProcessContext, branching: Branching) : InclusiveContext{
        val results = mutableMapOf<String, Boolean>()
        for((index, conditionalExecution) in branching.children.withIndex()){
            val conditional = conditionalExecution.children[0] as Conditional
            results["$index"] = conditional.condition?.invoke(processContext.processInstance)!!
        }
        return InclusiveContext(results, next = -1, counter = 0)
    }

}

class LoopHandler : NodeHandler(){
    override fun test(processContext: ProcessContext): Boolean {
        return processContext.node is Loop<*>
    }

    override fun handle(processContext: ProcessContext): HandlerResult {
        val loopContext : LoopContext
        if(processContext.input["LoopContext"] != null){
            loopContext = ObjectMapper().convertValue(processContext.input["LoopContext"], LoopContext::class.java)
        } else {
            return HandlerResult(LoopContext(0, false))
        }

        EndlessLoopPrevention.check(processContext, loopContext)

        val until = processContext.node!!.children.last() as Conditional
        val outcome = until.condition?.invoke(processContext.processInstance)!!
        return HandlerResult(LoopContext(loopContext.counter+1, outcome))
    }
}

class ExclusiveHandler : NodeHandler() {
    override fun test(processContext: ProcessContext): Boolean {
        val node = processContext.node
        return node is Branching && node.fork == Junction.One
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

class NoopHandler : NodeHandler() {
    override fun test(processContext: ProcessContext): Boolean {
        return true
    }

    override fun handle(processContext: ProcessContext): HandlerResult {
        // skip
        println("WARNING: no handler found for node ${processContext.node!!.javaClass}. Using default handler")
        return HandlerResult(toStringList(processContext.messageList))
    }
}