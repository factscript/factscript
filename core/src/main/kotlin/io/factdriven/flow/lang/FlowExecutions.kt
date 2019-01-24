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

    fun intent(name: String? = null): FlowReactionWithoutAction
    fun acceptance(name: String? = null): FlowReactionWithoutAction
    fun progress(name: String? = null): FlowReactionWithoutAction
    fun success(name: String? = null): FlowReactionWithoutAction
    fun fix(name: String? = null): FlowReactionWithoutAction
    fun failure(name: String? = null): FlowReactionWithoutAction

    fun <IN: Message, OUT: Message> intent(type: KClass<OUT>): FlowReactionAction<IN, OUT>
    fun <IN: Message, OUT: Message> acceptance(type: KClass<OUT>): FlowReactionAction<IN, OUT>
    fun <IN: Message, OUT: Message> progress(type: KClass<OUT>): FlowReactionAction<IN, OUT>
    fun <IN: Message, OUT: Message> success(type: KClass<OUT>): FlowReactionAction<IN, OUT>
    fun <IN: Message, OUT: Message> fix(type: KClass<OUT>): FlowReactionAction<IN, OUT>
    fun <IN: Message, OUT: Message> failure(type: KClass<OUT>): FlowReactionAction<IN, OUT>

    fun asDefinition(): FlowDefinition {
        return this as FlowDefinition
    }

}

interface FlowActivities<I: Aggregate> {

    operator fun invoke(mitigation: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit

    infix fun service(service: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit
    infix fun mitigation(mitigation: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit

}

class FlowExecutionImpl<I: Aggregate>(override val parent: FlowDefinition?): FlowDefinition, FlowExecution<I>, FlowActivities<I> {

    // Flow Definition

    override var flowExecutionType = FlowExecutionType.execution
    override val children = mutableListOf<FlowElement>()
    override lateinit var aggregateType: KClass<out Aggregate>

    override var flowElementType: String = ""
        get() {
            return when (flowExecutionType) {
                FlowExecutionType.service -> children[0].flowElementType
                else -> field
            }
        }

    val actions: List<FlowAction<I>> get() {
        @Suppress("UNCHECKED_CAST")
        return children.filter { it is FlowAction<*> } as List<FlowAction<I>>
    }

    val reactions: List<FlowReaction<I, *>> get() {
        @Suppress("UNCHECKED_CAST")
        return children.filter { it is FlowReaction<*, *> } as List<FlowReaction<I, *>>
    }

    // Basic Flow Execution<
    override val on: FlowReactions<I>
        get() {
            return FlowReactions(this)
        }

    override val create: FlowAction<I>
        get() {
            val node = FlowActionImpl<I, Any>(this)
            children.add(node)
            return node
        }

    override val execute: FlowActivities<I>
        get() {
        val node = FlowExecutionImpl<I>(this)
        children.add(node)
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

    override fun intent(name: String?): FlowReactionWithoutAction {
        return FlowReactionAction<Nothing, Nothing>(this, FlowActionType.Intent, name ?: flowElementType)
    }

    override fun <IN : Message, OUT : Message> intent(type: KClass<OUT>): FlowReactionAction<IN, OUT> {
        return FlowReactionAction(this, FlowActionType.Intent, type.simpleName!!)
    }

    override fun acceptance(name: String?): FlowReactionWithoutAction {
        return FlowReactionAction<Nothing, Nothing>(this, FlowActionType.Acceptance, name ?: flowElementType)
    }

    override fun <IN : Message, OUT : Message> acceptance(type: KClass<OUT>): FlowReactionAction<IN, OUT> {
        return FlowReactionAction(this, FlowActionType.Acceptance, type.simpleName!!)
    }

    override fun progress(name: String?): FlowReactionWithoutAction {
        return FlowReactionAction<Nothing, Nothing>(this, FlowActionType.Progress, name ?: flowElementType)
    }

    override fun <IN : Message, OUT : Message> progress(type: KClass<OUT>): FlowReactionAction<IN, OUT> {
        return FlowReactionAction(this, FlowActionType.Progress, type.simpleName!!)
    }

    override fun success(name: String?): FlowReactionWithoutAction {
        return FlowReactionAction<Nothing, Nothing>(this, FlowActionType.Success, name ?: flowElementType)
    }

    override fun <IN : Message, OUT : Message> success(type: KClass<OUT>): FlowReactionAction<IN, OUT> {
        return FlowReactionAction(this, FlowActionType.Success, type.simpleName!!)
    }

    override fun fix(name: String?): FlowReactionWithoutAction {
        return FlowReactionAction<Nothing, Nothing>(this, FlowActionType.Fix, name ?: flowElementType)
    }

    override fun <IN : Message, OUT : Message> fix(type: KClass<OUT>): FlowReactionAction<IN, OUT> {
        return FlowReactionAction(this, FlowActionType.Fix, type.simpleName!!)
    }

    override fun failure(name: String?): FlowReactionWithoutAction {
        return FlowReactionAction<Nothing, Nothing>(this, FlowActionType.Failure, name ?: flowElementType)
    }

    override fun <IN : Message, OUT : Message> failure(type: KClass<OUT>): FlowReactionAction<IN, OUT> {
        return FlowReactionAction(this, FlowActionType.Failure, type.simpleName!!)
    }

}

interface FlowReactionWithoutAction

data class FlowReactionAction<IN: Message, OUT: Message>(

    override val parent: FlowDefinition,
    override val flowActionType: FlowActionType = FlowActionType.Success,
    override val flowElementType: String = ""

): FlowReactionActionDefinition, FlowReactionWithoutAction {

    override var function: (Aggregate.(Message) -> Message)? = null

}
