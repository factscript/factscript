package io.factdriven.flow.lang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */


enum class FlowExecutionType {

    execution,
    mitigation,
    service,

}

interface FlowExecution<I : FlowInstance>: FlowElement, FlowActivities<I> {

    val on: FlowReactions<I>
    val create: FlowAction<I>

    val execute: FlowActivities<I>
    val select: FlowSelections<I>

    var status: I

    fun <M: FlowMessage> type(type: KClass<M>): DefaultFlowMessagePattern<M>
    fun <M: FlowMessage> pattern(pattern: M): DefaultFlowMessagePattern<M>

    fun <M: FlowMessage> intent(name: String): FlowReactionAction<M>
    fun <M: FlowMessage> acceptance(name: String): FlowReactionAction<M>
    fun <M: FlowMessage> progress(name: String): FlowReactionAction<M>
    fun <M: FlowMessage> success(name: String): FlowReactionAction<M>
    fun <M: FlowMessage> fix(name: String): FlowReactionAction<M>
    fun <M: FlowMessage> failure(name: String): FlowReactionAction<M>

    fun execute(definition: FlowExecution<I>.() -> Unit): FlowExecution<I>

    fun asDefinition(): FlowExecutionDefinition {
        return this as FlowExecutionDefinition
    }

}

interface FlowActivities<I: FlowInstance> {

    infix fun service(service: FlowExecution<I>.() -> Unit): FlowExecution<I>
    infix fun mitigation(mitigation: FlowExecution<I>.() -> Unit): FlowExecution<I>

}

class FlowExecutionImpl<I: FlowInstance>: FlowExecutionDefinition, FlowExecution<I>, FlowActivities<I> {

    // Flow Definition

    override lateinit var status: I
    override var executionType = FlowExecutionType.execution
    override val elements = mutableListOf<FlowElement>()
    override lateinit var instanceType: KClass<out FlowInstance>

    override var name: String = ""
        get() {
            return when (executionType) {
                FlowExecutionType.service -> elements[0].name
                else -> field
            }
        }

    // Basic Flow Execution

    override val on: FlowReactions<I>
        get() {
            return FlowReactions(this)
        }

    override val create: FlowAction<I>
        get() {
            val node = FlowActionImpl<I>()
            elements.add(node)
            return node
        }

    override val execute: FlowActivities<I>
        get() {
        val node = FlowExecutionImpl<I>()
        elements.add(node)
        return node
    }

    override val select: FlowSelections<I> get() = TODO()

    // Sub Flow Execution Factories

    override infix fun service(service: FlowExecution<I>.() -> Unit): FlowExecution<I> {
        executionType = FlowExecutionType.service
        this.apply(service)
        return this
    }

    override fun execute(definition: FlowExecution<I>.() -> Unit): FlowExecution<I> = TODO()

    override infix fun mitigation(mitigation: FlowExecution<I>.() -> Unit): FlowExecution<I> {
        executionType = FlowExecutionType.mitigation
        this.apply(mitigation)
        return this
    }

    // Message Pattern Factories

    override fun <M: FlowMessage> type(type: KClass<M>): DefaultFlowMessagePattern<M> {
        return DefaultFlowMessagePattern(type)
    }

    override fun <M: FlowMessage> pattern(pattern: M): DefaultFlowMessagePattern<M> {
        TODO()
    }

    // Action as Reaction Factories

    override fun <M: FlowMessage> intent(name: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.intent, this.name)
    }

    override fun <M: FlowMessage> acceptance(name: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.acceptance, name)
    }

    override fun <M: FlowMessage> progress(name: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.progress, name)
    }

    override fun <M: FlowMessage> success(name: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.success, name)
    }

    override fun <M: FlowMessage> fix(name: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.fix, name)
    }

    override fun <M: FlowMessage> failure(name: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.failure, name)
    }

}

data class DefaultFlowMessagePattern<M: FlowMessage>(override val type: KClass<out M>) : FlowMessagePattern
data class FlowReactionAction<M: FlowMessage>(val type: FlowActionType = FlowActionType.success, val id: String = "")
