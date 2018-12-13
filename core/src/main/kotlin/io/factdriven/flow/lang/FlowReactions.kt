package io.factdriven.flow.lang

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
enum class FlowReactionType {

    Message

}

class FlowReactions<I: FlowInstance>(val parent: FlowDefinition) {

    infix fun <M: Any> message(type: KClass<M>): FlowMessageReaction<I, M> {

        val reaction = FlowMessageReactionImpl<I, M>(type)
        parent.elements.add(reaction as FlowElement)
        return reaction

    }

    infix fun compensation(execution: FlowExecution<I>.() -> Unit): FlowExecution<I> {
        TODO()
    }

    infix fun timeout(timer: String): FlowReaction<I, Any> {
        TODO()
    }

    infix fun timeout(timer: () -> String): FlowReaction<I, Any> {
        TODO()
    }

}

interface FlowReaction<I: FlowInstance, A: Any> {

    infix fun create(action: FlowReactionAction<A>): ActionableFlowReaction<I, A>
    infix fun execute(execution: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit

    fun asDefinition(): FlowReactionDefinition {
        return this as FlowReactionDefinition
    }

}

interface FlowMessageReaction<I: FlowInstance, M: FlowMessagePayload> : FlowReaction<I, M> {

    infix fun having(property: String): MatchableFlowMessageReaction<I, M>
    infix fun supporting(assertion: I.(M) -> Boolean): FlowMessageReaction<I, M>

    override fun asDefinition(): FlowMessageReactionDefinition {
        return this as FlowMessageReactionDefinition
    }

}

interface MatchableFlowMessageReaction<I: FlowInstance, M: FlowMessagePayload> {

    infix fun match(value: I.() -> Any?): FlowMessageReaction<I, M>

}

interface ActionableFlowReaction<I: FlowInstance, A: Any> {

    infix fun by(reaction: I.(A) -> FlowMessagePayload)

}

abstract class FlowReactionImpl<I: FlowInstance, A: Any>(override var name: String): FlowReactionDefinition, FlowReaction<I, A>, ActionableFlowReaction<I, A> {

    // Flow Reaction Definition

    override var actionType = FlowActionType.Progress
    override var reactionType = FlowReactionType.Message
    override var function: (FlowInstance.(Any) -> FlowMessagePayload)? = null

    // Flow Action as Reaction Factory

    override infix fun create(action: FlowReactionAction<A>): ActionableFlowReaction<I, A> {
        this.actionType = action.type
        this.name = action.id
        return this
    }

    override fun execute(execution: FlowExecution<I>.() -> Unit): FlowExecution<I>.() -> Unit {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun by(reaction: I.(A) -> FlowMessagePayload) {
        @Suppress("UNCHECKED_CAST")
        this.function = reaction as FlowInstance.(Any) -> FlowMessagePayload
    }

}

class FlowMessageReactionImpl<I: FlowInstance, M: FlowMessagePayload>(override val type: KClass<M>): FlowMessageReactionDefinition, FlowReactionImpl<I, M>(type.simpleName!!), FlowMessageReaction<I, M>, MatchableFlowMessageReaction<I, M> {

    override val keys = mutableListOf<FlowMessageProperty>()
    override val values = mutableListOf<FlowInstance.() -> Any?>()

    init {
        reactionType = FlowReactionType.Message
    }

    // Message Patterns Refiner

    override fun having(property: String): MatchableFlowMessageReaction<I, M> {
        assert(type.java.kotlin.memberProperties.find { it.name == property } != null)
            { "Message type '${type.simpleName}' does not have property '${property}'!" }
        keys.add(property)
        return this
    }

    override fun match(value: I.() -> Any?): FlowMessageReaction<I, M> {
        @Suppress("UNCHECKED_CAST")
        values.add(value as FlowInstance.() -> Any?)
        return this
    }

    override infix fun supporting(assertion: I.(M) -> Boolean): FlowMessageReaction<I, M> {
        TODO()
    }

}
