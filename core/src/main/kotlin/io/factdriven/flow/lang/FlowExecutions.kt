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

interface FlowExecution<I : Aggregate>: FlowElement, FlowActivities<I> {

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

interface FlowActivities<I: Aggregate> {

    operator fun invoke(mitigation: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit

    infix fun service(service: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit
    infix fun mitigation(mitigation: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit

}

class FlowExecutionImpl<I: Aggregate>(override val container: FlowDefinition?): FlowDefinition, FlowExecution<I>, FlowActivities<I> {

    // Flow Definition

    override var flowExecutionType = FlowExecutionType.execution
    override val flowElements = mutableListOf<FlowElement>()
    override lateinit var aggregateType: KClass<out Aggregate>

    override var flowElementType: String = ""
        get() {
            return when (flowExecutionType) {
                FlowExecutionType.service -> flowElements[0].flowElementType
                else -> field
            }
        }

    val actions: List<FlowAction<I>> get() {
        @Suppress("UNCHECKED_CAST")
        return flowElements.filter { it is FlowAction<*> } as List<FlowAction<I>>
    }

    val reactions: List<FlowReaction<I, *>> get() {
        @Suppress("UNCHECKED_CAST")
        return flowElements.filter { it is FlowReaction<*, *> } as List<FlowReaction<I, *>>
    }

    // Basic Flow Execution<
    override val on: FlowReactions<I>
        get() {
            return FlowReactions(this)
        }

    override val create: FlowAction<I>
        get() {
            val node = FlowActionImpl<I>(this)
            flowElements.add(node)
            return node
        }

    override val execute: FlowActivities<I>
        get() {
        val node = FlowExecutionImpl<I>(this)
        flowElements.add(node)
        return node
    }

    override val select: FlowSelections<I> get() = TODO()

    // Sub Flow Execution Factories

    override infix fun service(service: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit {
        flowExecutionType = FlowExecutionType.service
        this.apply(service)
        return service
    }

    override fun invoke(execution: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit {
        this.apply(execution)
        return execution
    }

    override infix fun mitigation(mitigation: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit {
        flowExecutionType = FlowExecutionType.mitigation
        this.apply(mitigation)
        return mitigation
    }

    // Action as Reaction Factories

    override fun <M: FlowMessagePayload> intent(name: String): FlowReactionAction<M> {
        return FlowReactionAction(this, FlowActionType.Intent, name)
    }

    override fun <M: FlowMessagePayload> acceptance(name: String): FlowReactionAction<M> {
        return FlowReactionAction(this, FlowActionType.Acceptance, name)
    }

    override fun <M: FlowMessagePayload> progress(name: String): FlowReactionAction<M> {
        return FlowReactionAction(this, FlowActionType.Progress, name)
    }

    override fun <M: FlowMessagePayload> success(name: String): FlowReactionAction<M> {
        return FlowReactionAction(this, FlowActionType.Success, name)
    }

    override fun <M: FlowMessagePayload> fix(name: String): FlowReactionAction<M> {
        return FlowReactionAction(this, FlowActionType.Fix, name)
    }

    override fun <M: FlowMessagePayload> failure(name: String): FlowReactionAction<M> {
        return FlowReactionAction(this, FlowActionType.Failure, name)
    }

}

data class FlowReactionAction<M: FlowMessagePayload>(

    override val container: FlowDefinition,
    override val flowActionType: FlowActionType = FlowActionType.Success,
    override val flowElementType: String = ""

): FlowReactionActionDefinition {

    override var function: (Aggregate.(Any) -> FlowMessagePayload)? = null

}
