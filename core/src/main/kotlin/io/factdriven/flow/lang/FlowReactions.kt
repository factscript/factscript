package io.factdriven.flow.lang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
enum class FlowReactionType {

    message

}

class FlowReactions<I: FlowInstance>(val parent: FlowExecutionDefinition) {

    infix fun <M: Any> message(listener: DefaultFlowMessagePattern<M>): FlowMessageReaction<I, M> {

        val reaction = FlowMessageReactionImpl<I, M>(listener)
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

    infix fun having(key: () -> Pair<String, Any>): FlowMessageReaction<I, M>
    infix fun supporting(assertion: (M) -> Boolean): FlowMessageReaction<I, M>

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

class FlowMessageReactionImpl<I: FlowInstance, M: FlowMessage>(override val messagePattern: DefaultFlowMessagePattern<M>): FlowMessageReactionDefinition, FlowReactionImpl<I, M>(messagePattern.type.simpleName!!), FlowMessageReaction<I, M> {

    init {
        reactionType = FlowReactionType.message
    }

    // Message Patterns Refiner

    override infix fun having(key: () -> Pair<String, Any>): FlowMessageReactionImpl<I, M> {
        TODO()
    }

    override infix fun supporting(assertion: (M) -> Boolean): FlowMessageReactionImpl<I, M> {
        TODO()
    }

}
