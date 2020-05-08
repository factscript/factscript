package io.factdriven.language.execution.aws.lambda

import com.amazonaws.services.lambda.runtime.Context
import io.factdriven.execution.Fact
import io.factdriven.execution.Message
import io.factdriven.execution.newInstance
import io.factdriven.language.definition.Flow
import io.factdriven.language.definition.Node
import io.factdriven.language.execution.aws.example.function.RetrievePayment
import java.util.ArrayList
import java.util.stream.Collectors

data class ProcessSettings (val maximumLoopCycles: Int = 150)

abstract class LambdaContext constructor(definition: Flow, val processSettings: ProcessSettings = ProcessSettings()) {
    enum class State {
        INITIALIZATION, EXECUTION
    }

    abstract val state: State
    val stateMachine : StateMachine
    val definition: Flow

    init {
        this.stateMachine = StateMachine(name(definition))
        this.definition = definition
    }

    private fun name(flow: Flow) : String {
        return "${flow.description.replace(" ", "-")}-StateMachine"
    }

    companion object {
        fun of(definition: Flow, input: Map<String, *>, context: Context) : LambdaContext{
            return if(isInitialization(input)) {
                LambdaInitializationContext(definition, context)
            } else {
                ProcessContext(definition, input, context)
            }
        }

        private fun isInitialization(input: Any?) = input is Map<*, *> && input.isEmpty()
    }
}

class LambdaInitializationContext(definition: Flow, context: Context) : LambdaContext(definition){
    override val state: State
        get() = State.INITIALIZATION

}

class ProcessContext(definition: Flow, val input: Map<String, *>, context: Context): LambdaContext(definition) {

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
        this.processInstance = messageList.newInstance(definition.entity)
    }

    fun toMessageList(): MutableList<Message> {
        return toMessageList(input["History"] as ArrayList<String>?)
    }
    fun toMessageList(list: List<String>?): MutableList<Message>{
        if (list == null) {
            val init = RetrievePayment("a", "b", 1f)
            return mutableListOf(createMessage(init))
        }
        return (list as ArrayList<String>).stream()
                .map { s -> Message.fromJson(s) }
                .collect(Collectors.toList())
    }

    private fun createMessage(fact: Any): Message {
        return Message(fact.javaClass::class, Fact(fact))
    }
}

data class StateMachine(val name: String)