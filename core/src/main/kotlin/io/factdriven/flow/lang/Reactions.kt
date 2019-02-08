package io.factdriven.flow.lang

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
enum class ReactionClassifier {

    Message

}

class ReactionOptions<I: Entity>(val parent: DefinedFlow<*>) {

    infix fun <M: Any> message(type: KClass<M>): FlowMessageReaction<I, M> {

        val reaction = FlowMessageReactionImpl<I, M, Any>(parent, type)
        (parent.children as MutableList).add(reaction as Node) // TODO clean
        return reaction

    }

    infix fun compensation(execution: Flow<I>.() -> Unit): Flow<I> {
        TODO()
    }

    infix fun timeout(timer: String): FlowReaction<I, Any> {
        TODO()
    }

    infix fun timeout(timer: () -> String): FlowReaction<I, Any> {
        TODO()
    }

}

interface FlowReaction<I: Entity, IN: Fact> {

    infix fun <OUT: Fact> create(action: FlowReactionActionImpl<IN, OUT>): ActionableFlowReaction<I, IN, OUT>
    infix fun create(action: FlowReactionWithoutAction): FlowReactionWithoutAction
    infix fun execute(execution: Flow<I>.() -> Unit): Flow<I>.() -> Unit

    fun asDefinition(): DefinedReaction {
        return this as DefinedReaction
    }

}

interface FlowMessageReaction<I: Entity, M: Fact> : FlowReaction<I, M> {

    infix fun having(property: String): MatchableFlowMessageReaction<I, M>
    infix fun supporting(assertion: I.(M) -> Boolean): FlowMessageReaction<I, M>

    override fun asDefinition(): DefinedMessageReaction {
        return this as DefinedMessageReaction
    }

}

interface MatchableFlowMessageReaction<I: Entity, M: Fact> {

    infix fun match(value: I.() -> Any?): FlowMessageReaction<I, M>

}

interface ActionableFlowReaction<I: Entity, IN: Fact, OUT: Fact> {

    infix fun by(reaction: I.(IN) -> OUT?)

}

abstract class FlowReactionImpl<I: Entity, IN: Fact, OUT: Fact>(override val parent: DefinedFlow<*>, override var name: String): DefinedReaction, FlowReaction<I, IN>, ActionableFlowReaction<I, IN, OUT> {

    // Flow Reaction Definition

    override var classifier = ReactionClassifier.Message
    override var action: FlowReactionActionImpl<*, *>? = null

    // Flow Reaction Action Factory

    override infix fun <OUT: Fact> create(action: FlowReactionActionImpl<IN, OUT>): ActionableFlowReaction<I, IN, OUT> {
        this.action = action
        return this as ActionableFlowReaction<I, IN, OUT>
    }

    override fun create(action: FlowReactionWithoutAction): FlowReactionWithoutAction {
        this.action = action as FlowReactionActionImpl<*, *>
        return action
    }

    override fun execute(execution: Flow<I>.() -> Unit): Flow<I>.() -> Unit {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun by(reaction: I.(IN) -> OUT?) {
        @Suppress("UNCHECKED_CAST")
        this.action!!.function = reaction as Entity.(Any) -> Fact
    }

}

class FlowMessageReactionImpl<I: Entity, IN: Fact, OUT: Fact>(override val parent: DefinedFlow<*>, override val factType: KClass<IN>): DefinedMessageReaction, FlowReactionImpl<I, IN, OUT>(parent, factType.simpleName!!), FlowMessageReaction<I, IN>, MatchableFlowMessageReaction<I, IN> {

    override val properties = mutableListOf<Property>()
    override val values = mutableListOf<Entity?.() -> Fact?>()

    init {
        classifier = ReactionClassifier.Message
        FactTypes.add(factType)
    }

    // Message Patterns Refiner

    override fun having(property: String): MatchableFlowMessageReaction<I, IN> {
        assert(factType.java.kotlin.memberProperties.find { it.name == property } != null)
            { "Message actionType '${factType.simpleName}' does not have property '${property}'!" }
        properties.add(property)
        return this
    }

    override fun match(value: I.() -> Any?): FlowMessageReaction<I, IN> {
        @Suppress("UNCHECKED_CAST")
        values.add(value as Entity?.() -> Any?)
        return this
    }

    override infix fun supporting(assertion: I.(IN) -> Boolean): FlowMessageReaction<I, IN> {
        TODO()
    }

}
