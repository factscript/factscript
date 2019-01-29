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

    fun <IN: Fact, OUT: Fact> intent(type: KClass<OUT>): FlowReactionActionImpl<IN, OUT>
    fun <IN: Fact, OUT: Fact> acceptance(type: KClass<OUT>): FlowReactionActionImpl<IN, OUT>
    fun <IN: Fact, OUT: Fact> progress(type: KClass<OUT>): FlowReactionActionImpl<IN, OUT>
    fun <IN: Fact, OUT: Fact> success(type: KClass<OUT>): FlowReactionActionImpl<IN, OUT>
    fun <IN: Fact, OUT: Fact> fix(type: KClass<OUT>): FlowReactionActionImpl<IN, OUT>
    fun <IN: Fact, OUT: Fact> failure(type: KClass<OUT>): FlowReactionActionImpl<IN, OUT>

    fun asDefinition(): FlowDefinition<I> {
        return this as FlowDefinition<I>
    }

}

interface FlowActivities<I: Aggregate> {

    operator fun invoke(mitigation: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit

    infix fun service(service: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit
    infix fun mitigation(mitigation: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit

}

class FlowExecutionImpl<I: Aggregate>(override val parent: FlowDefinition<*>?): FlowDefinition<I>, FlowExecution<I>, FlowActivities<I> {

    // Flow Definition

    override var executionType = FlowExecutionType.execution
    override val children = mutableListOf<FlowElement>()
    override lateinit var aggregateType: KClass<out Aggregate>

    override var name: String = ""
        get() {
            return when (executionType) {
                FlowExecutionType.service -> children[0].name
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

    override fun intent(name: String?): FlowReactionWithoutAction {
        return FlowReactionActionImpl<Nothing, Nothing>(this, FlowActionType.Intent, name = name ?: this.name)
    }

    override fun <IN : Fact, OUT : Fact> intent(type: KClass<OUT>): FlowReactionActionImpl<IN, OUT> {
        return FlowReactionActionImpl(this, FlowActionType.Intent, type)
    }

    override fun acceptance(name: String?): FlowReactionWithoutAction {
        return FlowReactionActionImpl<Nothing, Nothing>(this, FlowActionType.Acceptance, name = name ?: this.name)
    }

    override fun <IN : Fact, OUT : Fact> acceptance(type: KClass<OUT>): FlowReactionActionImpl<IN, OUT> {
        return FlowReactionActionImpl(this, FlowActionType.Acceptance, type)
    }

    override fun progress(name: String?): FlowReactionWithoutAction {
        return FlowReactionActionImpl<Nothing, Nothing>(this, FlowActionType.Progress, name = name ?: this.name)
    }

    override fun <IN : Fact, OUT : Fact> progress(type: KClass<OUT>): FlowReactionActionImpl<IN, OUT> {
        return FlowReactionActionImpl(this, FlowActionType.Progress, type)
    }

    override fun success(name: String?): FlowReactionWithoutAction {
        return FlowReactionActionImpl<Nothing, Nothing>(this, FlowActionType.Success, name = name ?: this.name)
    }

    override fun <IN : Fact, OUT : Fact> success(type: KClass<OUT>): FlowReactionActionImpl<IN, OUT> {
        return FlowReactionActionImpl(this, FlowActionType.Success, type)
    }

    override fun fix(name: String?): FlowReactionWithoutAction {
        return FlowReactionActionImpl<Nothing, Nothing>(this, FlowActionType.Fix, name = name ?: this.name)
    }

    override fun <IN : Fact, OUT : Fact> fix(type: KClass<OUT>): FlowReactionActionImpl<IN, OUT> {
        return FlowReactionActionImpl(this, FlowActionType.Fix, type)
    }

    override fun failure(name: String?): FlowReactionWithoutAction {
        return FlowReactionActionImpl<Nothing, Nothing>(this, FlowActionType.Failure, name = name ?: this.name)
    }

    override fun <IN : Fact, OUT : Fact> failure(type: KClass<OUT>): FlowReactionActionImpl<IN, OUT> {
        return FlowReactionActionImpl(this, FlowActionType.Failure, type)
    }

}

interface FlowReactionWithoutAction

class FlowReactionActionImpl<IN: Fact, OUT: Fact>(

    override val parent: FlowDefinition<*>,
    override val actionType: FlowActionType = FlowActionType.Success,
    override var messageType: FactType<*>? = null,
    override val name: String = messageType?.simpleName ?: ""

): FlowReactionActionDefinition, FlowReactionWithoutAction {

    override var function: (Aggregate.(Fact) -> Fact)? = null

}
