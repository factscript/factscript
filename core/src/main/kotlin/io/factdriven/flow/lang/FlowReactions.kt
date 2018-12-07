package io.factdriven.flow.lang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
enum class FlowReactionType {

    message

}

class FlowReactions<I: FlowInstance>(val parent: FlowDefinition) {

    infix fun <M: Any> message(pattern: FlowMessagePattern<M>): FlowMessageReaction<I, M> {

        val reaction = FlowMessageReactionImpl<I, M>(pattern)
        parent.elements.add(reaction)
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

    infix fun create(action: FlowReactionAction<A>): ClassifiedFlowReaction<I, A>
    infix fun execute(execution: FlowExecution<I>): FlowExecution<I>

    fun asDefinition(): FlowReactionDefinition {
        return this as FlowReactionDefinition
    }

}

interface FlowMessageReaction<I: FlowInstance, M: FlowMessage> : FlowReaction<I, M> {

    infix fun having(key: String): FlowMessageReactionMatch<I, M>
    infix fun supporting(assertion: (M) -> Boolean): FlowMessageReaction<I, M>

}

interface FlowMessageReactionMatch<I: FlowInstance, M: FlowMessage> {

    infix fun match(match: () -> Any): FlowMessageReaction<I, M>

}

interface ClassifiedFlowReaction<I: FlowInstance, A: Any> {

    infix fun by(reaction: (A) -> FlowMessage)

}

abstract class FlowReactionImpl<I: FlowInstance, A: Any>(override var name: String): FlowReactionDefinition, FlowReaction<I, A>, ClassifiedFlowReaction<I, A> {

    // Flow Reaction Definition

    override var actionType = FlowActionType.progress
    override var reactionType = FlowReactionType.message
    override var function: ((Any) -> FlowMessage)? = null

    // Flow Action as Reaction Factory

    override infix fun create(action: FlowReactionAction<A>): ClassifiedFlowReaction<I, A> {
        this.actionType = action.type
        this.name = action.id
        return this
    }

    // Flow Execution as Reaction Factory

    override infix fun execute(execution: FlowExecution<I>): FlowExecution<I> {
        TODO()
    }

    override fun by(reaction: (A) -> FlowMessage) {
        @Suppress("UNCHECKED_CAST")
        this.function = reaction as (Any) -> FlowMessage
    }

}

class FlowMessageReactionImpl<I: FlowInstance, M: FlowMessage>(override val messagePattern: FlowMessagePattern<M>): FlowMessageReactionDefinition, FlowReactionImpl<I, M>(messagePattern.type.simpleName!!), FlowMessageReaction<I, M>, FlowMessageReactionMatch<I, M> {

    init {
        reactionType = FlowReactionType.message
    }

    // Message Patterns Refiner

    override fun having(key: String): FlowMessageReactionMatch<I, M> {
        TODO()
    }

    override fun match(match: () -> Any): FlowMessageReaction<I, M> {
        TODO()
    }

    override infix fun supporting(assertion: (M) -> Boolean): FlowMessageReactionImpl<I, M> {
        TODO()
    }

}
