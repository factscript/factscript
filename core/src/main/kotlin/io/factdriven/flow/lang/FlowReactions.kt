package io.factdriven.flow.lang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
enum class FlowReactionType {

    Message

}

class FlowReactions<I: FlowInstance>(val parent: FlowDefinition) {

    infix fun <M: Any> message(reaction: TypedFlowMessageReaction<M>): FlowMessageReaction<I, M> {

        val r = reaction as FlowMessageReaction<I, M>
        parent.elements.add(reaction as FlowElement)
        return reaction

    }

    infix fun compensation(execution: FlowExecution<I>): FlowExecution<I> {
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
    infix fun execute(execution: FlowExecution<I>): FlowExecution<I>

    fun asDefinition(): FlowReactionDefinition {
        return this as FlowReactionDefinition
    }

}

interface FlowMessageReaction<I: FlowInstance, M: FlowMessage> : FlowReaction<I, M> {

    infix fun having(key: String): MatchableFlowMessageReaction<I, M>
    infix fun supporting(assertion: (M) -> Boolean): FlowMessageReaction<I, M>

    override fun asDefinition(): FlowMessageReactionDefinition {
        return this as FlowMessageReactionDefinition
    }

}

interface TypedFlowMessageReaction<M: FlowMessage>

interface MatchableFlowMessageReaction<I: FlowInstance, M: FlowMessage> {

    infix fun match(match: I.() -> Any?): FlowMessageReaction<I, M>

}

interface ActionableFlowReaction<I: FlowInstance, A: Any> {

    infix fun by(reaction: I.(A) -> FlowMessage)

}

abstract class FlowReactionImpl<I: FlowInstance, A: Any>(override var name: String): FlowReactionDefinition, FlowReaction<I, A>, ActionableFlowReaction<I, A> {

    // Flow Reaction Definition

    override var actionType = FlowActionType.Progress
    override var reactionType = FlowReactionType.Message
    override var function: (FlowInstance.(Any) -> FlowMessage)? = null

    // Flow Action as Reaction Factory

    override infix fun create(action: FlowReactionAction<A>): ActionableFlowReaction<I, A> {
        this.actionType = action.type
        this.name = action.id
        return this
    }

    // Flow Execution as Reaction Factory

    override infix fun execute(execution: FlowExecution<I>): FlowExecution<I> {
        TODO()
    }

    override fun by(reaction: I.(A) -> FlowMessage) {
        @Suppress("UNCHECKED_CAST")
        this.function = reaction as FlowInstance.(Any) -> FlowMessage
    }

}

class FlowMessageReactionImpl<I: FlowInstance, M: FlowMessage>(override val type: KClass<M>): FlowMessageReactionDefinition, FlowReactionImpl<I, M>(type.simpleName!!), FlowMessageReaction<I, M>, TypedFlowMessageReaction<M>, MatchableFlowMessageReaction<I, M> {

    init {
        reactionType = FlowReactionType.Message
    }

    // Message Patterns Refiner

    override fun having(key: String): MatchableFlowMessageReaction<I, M> {
        TODO()
    }

    override fun match(match: I.() -> Any?): FlowMessageReaction<I, M> {
        TODO()
    }

    override infix fun supporting(assertion: (M) -> Boolean): FlowMessageReactionImpl<I, M> {
        TODO()
    }

}
