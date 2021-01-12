package org.factscript.language.execution.aws.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.factscript.execution.Fact
import org.factscript.execution.Message
import org.factscript.execution.newInstance
import org.factscript.language.definition.Flow
import org.factscript.language.definition.Node
import org.factscript.language.execution.aws.translation.FlowTranslator
import org.factscript.language.execution.aws.translation.context.LambdaFunction
import org.factscript.language.execution.aws.translation.context.SnsContext
import java.util.*
import java.util.stream.Collectors

data class ProcessSettings (val maximumLoopCycles: Int = 5)

abstract class LambdaContext constructor(definition: Flow, val input: Map<String, *>, val context: Context, val processSettings: ProcessSettings = ProcessSettings()) {
    enum class State {
        INITIALIZATION, EXECUTION, CATCHING
    }

    abstract val state: State
    val stateMachine : StateMachine
    val definition: Flow

    init {
        this.stateMachine = StateMachine(name(definition)+"-tmp")
        this.definition = definition
    }

    private fun name(flow: Flow) : String {
        return "${flow.description.replace(" ", "-")}-StateMachine"
    }

    companion object {
        fun of(definition: Flow, input: Map<String, *>, context: Context) : LambdaContext{
            return if(LambdaInitializationContext.isInitialization(input)) {
                LambdaInitializationContext(definition, input, context)
            } else if(EventContext.isEvent(input)){
                EventContext(definition, input, context)
            } else {
                ProcessContext(definition, input, context)
            }
        }
    }
}
class EventContext(definition: Flow, input: Map<String, *>, context: Context) : LambdaContext(definition, input, context){
    override val state: State
        get() = State.CATCHING

    val event : Any
    val node : Node

    init {
        val record = input["Records"] ?: throw IllegalArgumentException("Not a SNS message")
        val sns = (record as ArrayList<Map<String, Any>>)[0]["Sns"] as Map<String, String>
        val subject = sns["Subject"]
        val message = sns["Message"]
        val objectMapper = ObjectMapper().registerModule(KotlinModule())

        event = objectMapper.readValue(message, Class.forName(subject))

        val translationResult = FlowTranslator.translate(definition, LambdaFunction(context.functionName), SnsContext.fromLambdaArn(context.invokedFunctionArn))
        val snsContext = translationResult.translationContext.snsContext

        println("possible event nodes: ${snsContext.consumingNodes}")
        node = snsContext.consumingNodes.first{node -> node.consuming == event::class} // TODO multiple possible
    }

    companion object {
        fun isEvent(input: Map<String, *>) : Boolean{
            val record = input["Records"] ?: return false
            val sns = (record as ArrayList<Map<String, Any>>)[0]["Sns"] as Map<String, String>
            val subject = sns["Subject"]
            val message = sns["Message"]
            return try {
                Class.forName(subject) // heuristic, better search definition
                true
            } catch (e: Exception){
                println("not a known event $e")
                false
            }
        }
    }
}
class LambdaInitializationContext(definition: Flow, input: Map<String, *>, context: Context) : LambdaContext(definition, input, context){
    override val state: State
        get() = State.INITIALIZATION

    companion object {
        fun isInitialization(input: Any?) = input is Map<*, *> && input.isEmpty()
    }
}

class ProcessContext(definition: Flow, input: Map<String, *>, context: Context): LambdaContext(definition, input, context) {

    val token : String
    val node : Node?
    val messageList : MutableList<Message>
    val processInstance : Any
    val phase : String?

    override val state: State
        get() = State.EXECUTION

    init {
        this.token = input["TaskToken"] as String
        if(input["id"] != null) {
            this.node = definition.get(input["id"] as String)
        } else {
            this.node = null
        }
        if(input["Phase"] != null){
            phase = input["Phase"] as String
        } else {
            phase = null
        }
        this.messageList = toMessageList()
        println(messageList)
        println(definition.entity)
        this.processInstance = messageList.newInstance(definition.entity)
    }

    fun toMessageList(): MutableList<Message> {

        var messageSource = input["History"]

        if(messageSource is LinkedHashMap<*, *> && messageSource["Error"] != null){
                messageSource = ObjectMapper().readValue(messageSource["Cause"] as String, List::class.java)
        }

        return toMessageList(messageSource as List<String>?)
    }
    fun toMessageList(list: List<String>?): MutableList<Message>{
        return (list as ArrayList<String>).stream()
                .map { s -> Message.fromJson(s) }
                .collect(Collectors.toList())
    }

    private fun createMessage(fact: Any): Message {
        return Message(fact.javaClass::class, Fact(fact))
    }
}

data class StateMachine(val name: String)