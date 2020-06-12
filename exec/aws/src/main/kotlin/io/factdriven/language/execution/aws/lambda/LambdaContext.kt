package io.factdriven.language.execution.aws.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.factdriven.execution.Fact
import io.factdriven.execution.Message
import io.factdriven.execution.newInstance
import io.factdriven.language.definition.*
import io.factdriven.language.execution.aws.example.function.RetrievePayment
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.HashSet

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

        val possibleEventNodes = HashSet<Consuming>()
        possibleEventNodes.add(definition.find(Promising::class)!!)
        possibleEventNodes.addAll(definition.filter(Correlating::class))
        println("possible event nodes: $possibleEventNodes")
        node = possibleEventNodes.first{node -> node.consuming == event::class}
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

    override val state: State
        get() = State.EXECUTION

    init {
        this.token = input["TaskToken"] as String
        if(input["id"] != null) {
            this.node = definition.get(input["id"] as String)
        } else {
            this.node = null
        }
        this.messageList = toMessageList()
        println(messageList)
        println(definition.entity)
        this.processInstance = messageList.newInstance(definition.entity)
    }

    fun toMessageList(): MutableList<Message> {
        return toMessageList(input["History"] as ArrayList<String>?)
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