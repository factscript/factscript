package io.factdriven.flow.lang

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
enum class ReactionClassifier {

    Message

}

class ReactionOptions<I: Entity>(val parent: Flow<*>) {

    infix fun <M: Any> message(type: KClass<M>): UnclassifiedFlowMessageReaction<I, M> {

        val reaction = FlowMessageReactionImpl<I, M, Any>(parent, type)
        (parent.children as MutableList).add(reaction as Node) // TODO clean
        return reaction

    }

    infix fun compensation(execution: UnclassifiedFlow<I>.() -> Unit): UnclassifiedFlow<I> {
        TODO()
    }

    infix fun timeout(timer: String): UnclassifiedFlowReaction<I, Any> {
        TODO()
    }

    infix fun timeout(timer: () -> String): UnclassifiedFlowReaction<I, Any> {
        TODO()
    }

}

interface UnclassifiedFlowReaction<I: Entity, IN: Fact> {

    infix fun <OUT: Fact> create(action: FlowReactionActionImpl<IN, OUT>): ActionableFlowReaction<I, IN, OUT>
    infix fun create(action: FlowReactionWithoutAction): FlowReactionWithoutAction
    infix fun execute(execution: UnclassifiedFlow<I>.() -> Unit): UnclassifiedFlow<I>.() -> Unit

    fun asDefinition(): Reaction {
        return this as Reaction
    }

}

interface UnclassifiedFlowMessageReaction<I: Entity, M: Fact> : UnclassifiedFlowReaction<I, M> {

    infix fun having(property: String): MatchableFlowMessageReaction<I, M>
    infix fun supporting(assertion: I.(M) -> Boolean): UnclassifiedFlowMessageReaction<I, M>

    override fun asDefinition(): MessageReaction {
        return this as MessageReaction
    }

}

interface MatchableFlowMessageReaction<I: Entity, M: Fact> {

    infix fun match(value: I.() -> Any?): UnclassifiedFlowMessageReaction<I, M>

}

interface ActionableFlowReaction<I: Entity, IN: Fact, OUT: Fact> {

    infix fun by(reaction: I.(IN) -> OUT?)

}

abstract class FlowReactionImpl<I: Entity, IN: Fact, OUT: Fact>(override val parent: Flow<*>, override var name: String): Reaction, UnclassifiedFlowReaction<I, IN>, ActionableFlowReaction<I, IN, OUT> {

    // UnclassifiedFlow Reaction Definition

    override var classifier = ReactionClassifier.Message
    override var action: FlowReactionActionImpl<*, *>? = null

    // UnclassifiedFlow Reaction Action Factory

    override infix fun <OUT: Fact> create(action: FlowReactionActionImpl<IN, OUT>): ActionableFlowReaction<I, IN, OUT> {
        this.action = action
        return this as ActionableFlowReaction<I, IN, OUT>
    }

    override fun create(action: FlowReactionWithoutAction): FlowReactionWithoutAction {
        this.action = action as FlowReactionActionImpl<*, *>
        return action
    }

    override fun execute(execution: UnclassifiedFlow<I>.() -> Unit): UnclassifiedFlow<I>.() -> Unit {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun by(reaction: I.(IN) -> OUT?) {
        @Suppress("UNCHECKED_CAST")
        this.action!!.function = reaction as Entity.(Any) -> Fact
    }

}

class FlowMessageReactionImpl<I: Entity, IN: Fact, OUT: Fact>(override val parent: Flow<*>, override val type: KClass<IN>): MessageReaction, FlowReactionImpl<I, IN, OUT>(parent, type.simpleName!!), UnclassifiedFlowMessageReaction<I, IN>, MatchableFlowMessageReaction<I, IN> {

    override val properties = mutableListOf<Property>()
    override val values = mutableListOf<Entity?.() -> Fact?>()

    init {
        classifier = ReactionClassifier.Message
        FactTypes.add(type)
    }

    // Message Patterns Refiner

    override fun having(property: String): MatchableFlowMessageReaction<I, IN> {
        assert(type.java.kotlin.memberProperties.find { it.name == property } != null)
            { "Message actionType '${type.simpleName}' does not have property '${property}'!" }
        properties.add(property)
        return this
    }

    override fun match(value: I.() -> Any?): UnclassifiedFlowMessageReaction<I, IN> {
        @Suppress("UNCHECKED_CAST")
        values.add(value as Entity?.() -> Any?)
        return this
    }

    override infix fun supporting(assertion: I.(IN) -> Boolean): UnclassifiedFlowMessageReaction<I, IN> {
        TODO()
    }

}
