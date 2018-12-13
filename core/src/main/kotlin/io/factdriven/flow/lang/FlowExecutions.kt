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

    fun <M: FlowMessagePayload> intent(name: String): FlowReactionAction<M>
    fun <M: FlowMessagePayload> acceptance(name: String): FlowReactionAction<M>
    fun <M: FlowMessagePayload> progress(name: String): FlowReactionAction<M>
    fun <M: FlowMessagePayload> success(name: String): FlowReactionAction<M>
    fun <M: FlowMessagePayload> fix(name: String): FlowReactionAction<M>
    fun <M: FlowMessagePayload> failure(name: String): FlowReactionAction<M>

    fun asDefinition(): FlowDefinition {
        return this as FlowDefinition
    }

}

interface FlowActivities<I: FlowInstance> {

    operator fun invoke(mitigation: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit

    infix fun service(service: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit
    infix fun mitigation(mitigation: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit

}

class FlowExecutionImpl<I: FlowInstance>: FlowDefinition, FlowExecution<I>, FlowActivities<I> {

    // Flow Definition

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

    val actions: List<FlowAction<I>> get() {
        @Suppress("UNCHECKED_CAST")
        return elements.filter { it is FlowAction<*> } as List<FlowAction<I>>
    }

    val reactions: List<FlowReaction<I, *>> get() {
        @Suppress("UNCHECKED_CAST")
        return elements.filter { it is FlowReaction<*, *> } as List<FlowReaction<I, *>>
    }

    // Basic Flow Execution<
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

    override infix fun service(service: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit {
        executionType = FlowExecutionType.service
        this.apply(service)
        return service
    }

    override fun invoke(execution: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit {
        this.apply(execution)
        return execution
    }

    override infix fun mitigation(mitigation: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit {
        executionType = FlowExecutionType.mitigation
        this.apply(mitigation)
        return mitigation
    }

    // Action as Reaction Factories

    override fun <M: FlowMessagePayload> intent(name: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.Intent, name)
    }

    override fun <M: FlowMessagePayload> acceptance(name: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.Acceptance, name)
    }

    override fun <M: FlowMessagePayload> progress(name: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.Progress, name)
    }

    override fun <M: FlowMessagePayload> success(name: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.Success, name)
    }

    override fun <M: FlowMessagePayload> fix(name: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.Fix, name)
    }

    override fun <M: FlowMessagePayload> failure(name: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.Failure, name)
    }

}

data class FlowReactionAction<M: FlowMessagePayload>(val type: FlowActionType = FlowActionType.Success, val id: String = "")
