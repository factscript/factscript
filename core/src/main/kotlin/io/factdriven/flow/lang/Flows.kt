package io.factdriven.flow.lang

import io.factdriven.flow.Flows

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */


enum class FlowClassifier {

    Execution,
    Mitigation,
    Service,

}

interface UnclassifiedFlow<ENTITY : Entity>:
    Node,
    FlowOptions<ENTITY>
{

    val on: ReactionOptions<ENTITY>
    val create: UnclassifiedAction<ENTITY>
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

    fun asDefinition(): Flow<ENTITY> {
        return this as Flow<ENTITY>
    }

}

interface FlowOptions<ENTITY: Entity> {

    operator fun invoke(mitigation: UnclassifiedFlow<ENTITY>.() -> Unit): UnclassifiedFlow<ENTITY>.() -> Unit
    infix fun service(service: UnclassifiedFlow<ENTITY>.() -> Unit): UnclassifiedFlow<ENTITY>.() -> Unit
    infix fun mitigation(mitigation: UnclassifiedFlow<ENTITY>.() -> Unit): UnclassifiedFlow<ENTITY>.() -> Unit

}

class FlowImpl<ENTITY: Entity> :
    UnclassifiedFlow<ENTITY>,
    FlowOptions<ENTITY>,
    Flow<ENTITY>
{

    // UnclassifiedFlow Definition

    override val parent: Flow<*>?

    constructor(parent: Flow<*>?) {
        this.parent = parent
        this.children = mutableListOf<Node>()
    }

    override var classifier = FlowClassifier.Execution
    override val children: MutableList<Node>
    override lateinit var type: EntityType<ENTITY>

    override var name: String = ""
        get() {
            return when (classifier) {
                FlowClassifier.Service -> children[0].name
                else -> field
            }
        }

    val actions: List<UnclassifiedAction<ENTITY>> get() {
        @Suppress("UNCHECKED_CAST")
        return children.filter { it is UnclassifiedAction<*> } as List<UnclassifiedAction<ENTITY>>
    }

    val reactions: List<UnclassifiedFlowReaction<ENTITY, *>> get() {
        @Suppress("UNCHECKED_CAST")
        return children.filter { it is UnclassifiedFlowReaction<*, *> } as List<UnclassifiedFlowReaction<ENTITY, *>>
    }

    // Basic UnclassifiedFlow Execution<
    override val on: ReactionOptions<ENTITY>
        get() {
            return ReactionOptions(this)
        }

    override val create: UnclassifiedAction<ENTITY>
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

    // Sub UnclassifiedFlow Execution Factories

    override infix fun service(service: UnclassifiedFlow<ENTITY>.() -> Unit): UnclassifiedFlow<ENTITY>.() -> Unit {
        classifier = FlowClassifier.Service
        this.apply(service)
        return service
    }

    override fun invoke(execution: UnclassifiedFlow<ENTITY>.() -> Unit): UnclassifiedFlow<ENTITY>.() -> Unit {
        this.apply(execution)
        return execution
    }

    override infix fun mitigation(mitigation: UnclassifiedFlow<ENTITY>.() -> Unit): UnclassifiedFlow<ENTITY>.() -> Unit {
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

    override val parent: Flow<*>,
    override val classifier: ActionClassifier = ActionClassifier.Success,
    override var type: FactType<*>? = null,
    override val name: String = type?.simpleName ?: ""

): ReactionAction,
    FlowReactionWithoutAction

{

    init {
        type?.let { FactTypes.add(it)}
    }

    override var function: (Entity.(Fact) -> Fact)? = null

}

