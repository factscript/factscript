package io.factdriven.flow.lang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */


enum class FlowClassifier {

    Execution,
    Mitigation,
    Service,

}

interface Flow<ENTITY : Entity>:
    Node,
    FlowOptions<ENTITY>
{

    val on: ReactionOptions<ENTITY>
    val create: Action<ENTITY>
    val execute: FlowOptions<ENTITY>
    val select: SelectOptions<ENTITY>

    fun intention(type: FactName? = null): FlowReactionWithoutAction
    fun progress(type: FactName? = null): FlowReactionWithoutAction
    fun success(type: FactName? = null): FlowReactionWithoutAction
    fun failure(type: FactName? = null): FlowReactionWithoutAction
    fun fix(type: FactName? = null): FlowReactionWithoutAction

    fun <IN: Fact, OUT: Fact> intention(type: FactType<OUT>): FlowReactionActionImpl<IN, OUT>
    fun <IN: Fact, OUT: Fact> progress(type: FactType<OUT>): FlowReactionActionImpl<IN, OUT>
    fun <IN: Fact, OUT: Fact> success(type: FactType<OUT>): FlowReactionActionImpl<IN, OUT>
    fun <IN: Fact, OUT: Fact> fix(type: FactType<OUT>): FlowReactionActionImpl<IN, OUT>
    fun <IN: Fact, OUT: Fact> failure(type: FactType<OUT>): FlowReactionActionImpl<IN, OUT>

    fun asDefinition(): DefinedFlow<ENTITY> {
        return this as DefinedFlow<ENTITY>
    }

}

interface FlowOptions<ENTITY: Entity> {

    operator fun invoke(mitigation: Flow<ENTITY>.() -> Unit): Flow<ENTITY>.() -> Unit
    infix fun service(service: Flow<ENTITY>.() -> Unit): Flow<ENTITY>.() -> Unit
    infix fun mitigation(mitigation: Flow<ENTITY>.() -> Unit): Flow<ENTITY>.() -> Unit

}

class FlowImpl<ENTITY: Entity> :
    Flow<ENTITY>,
    FlowOptions<ENTITY>,
    DefinedFlow<ENTITY>
{

    // Flow Definition

    override val parent: DefinedFlow<*>?

    constructor(parent: DefinedFlow<*>?) {
        this.parent = parent
        this.children = mutableListOf<Node>()
    }

    override var classifier = FlowClassifier.Execution
    override val children: MutableList<Node>
    override lateinit var entityType: EntityType<ENTITY>

    override var name: String = ""
        get() {
            return when (classifier) {
                FlowClassifier.Service -> children[0].name
                else -> field
            }
        }

    val actions: List<Action<ENTITY>> get() {
        @Suppress("UNCHECKED_CAST")
        return children.filter { it is Action<*> } as List<Action<ENTITY>>
    }

    val reactions: List<FlowReaction<ENTITY, *>> get() {
        @Suppress("UNCHECKED_CAST")
        return children.filter { it is FlowReaction<*, *> } as List<FlowReaction<ENTITY, *>>
    }

    // Basic Flow Execution<
    override val on: ReactionOptions<ENTITY>
        get() {
            return ReactionOptions(this)
        }

    override val create: Action<ENTITY>
        get() {
            val node = ActionImpl<ENTITY, Any>(this)
            children.add(node)
            return node
        }

    override val execute: FlowOptions<ENTITY>
        get() {
        val node = FlowImpl<ENTITY>(this)
        children.add(node)
        return node
    }

    override val select: SelectOptions<ENTITY> get() = TODO()

    // Sub Flow Execution Factories

    override infix fun service(service: Flow<ENTITY>.() -> Unit): Flow<ENTITY>.() -> Unit {
        classifier = FlowClassifier.Service
        this.apply(service)
        return service
    }

    override fun invoke(execution: Flow<ENTITY>.() -> Unit): Flow<ENTITY>.() -> Unit {
        this.apply(execution)
        return execution
    }

    override infix fun mitigation(mitigation: Flow<ENTITY>.() -> Unit): Flow<ENTITY>.() -> Unit {
        classifier = FlowClassifier.Mitigation
        this.apply(mitigation)
        return mitigation
    }

    // Action as Reaction Factories

    override fun intention(type: FactName?): FlowReactionWithoutAction {
        return FlowReactionActionImpl<Nothing, Nothing>(this, ActionClassifier.Intention, name = type ?: this.name)
    }

    override fun <IN : Fact, OUT : Fact> intention(type: FactType<OUT>): FlowReactionActionImpl<IN, OUT> {
        return FlowReactionActionImpl(this, ActionClassifier.Intention, type)
    }

    override fun progress(type: FactName?): FlowReactionWithoutAction {
        return FlowReactionActionImpl<Nothing, Nothing>(this, ActionClassifier.Progress, name = type ?: this.name)
    }

    override fun <IN : Fact, OUT : Fact> progress(type: FactType<OUT>): FlowReactionActionImpl<IN, OUT> {
        return FlowReactionActionImpl(this, ActionClassifier.Progress, type)
    }

    override fun success(type: FactName?): FlowReactionWithoutAction {
        return FlowReactionActionImpl<Nothing, Nothing>(this, ActionClassifier.Success, name = type ?: this.name)
    }

    override fun <IN : Fact, OUT : Fact> success(type: FactType<OUT>): FlowReactionActionImpl<IN, OUT> {
        return FlowReactionActionImpl(this, ActionClassifier.Success, type)
    }

    override fun fix(type: FactName?): FlowReactionWithoutAction {
        return FlowReactionActionImpl<Nothing, Nothing>(this, ActionClassifier.Fix, name = type ?: this.name)
    }

    override fun <IN : Fact, OUT : Fact> fix(type: FactType<OUT>): FlowReactionActionImpl<IN, OUT> {
        return FlowReactionActionImpl(this, ActionClassifier.Fix, type)
    }

    override fun failure(type: FactName?): FlowReactionWithoutAction {
        return FlowReactionActionImpl<Nothing, Nothing>(this, ActionClassifier.Failure, name = type ?: this.name)
    }

    override fun <IN : Fact, OUT : Fact> failure(type: FactType<OUT>): FlowReactionActionImpl<IN, OUT> {
        return FlowReactionActionImpl(this, ActionClassifier.Failure, type)
    }

}

interface FlowReactionWithoutAction

class FlowReactionActionImpl<IN: Fact, OUT: Fact>(

    override val parent: DefinedFlow<*>,
    override val classifier: ActionClassifier = ActionClassifier.Success,
    override var factType: FactType<*>? = null,
    override val name: String = factType?.simpleName ?: ""

): DefinedReactionAction,
    FlowReactionWithoutAction

{

    init {
        factType?.let { FactTypes.add(it)}
    }

    override var function: (Entity.(Fact) -> Fact)? = null

}

