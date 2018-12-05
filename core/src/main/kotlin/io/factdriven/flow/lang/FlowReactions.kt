package io.factdriven.flow.lang

import io.factdriven.flow.FlowMessage

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
enum class FlowReactionType {
    message
}

class FlowReactions<I: Any>(val parent: FlowExecutionImpl<I>) {

    infix fun <M: Any> message(listener: FlowMessagePattern<M>): FlowReactionImpl<I, M> {
        val reaction = FlowReactionImpl<I, M>()
        parent.nodes.add(reaction)
        reaction.reactionType = FlowReactionType.message
        reaction.listener = listener
        return reaction
    }

    infix fun compensation(definition: FlowExecutionImpl<I>.() -> Unit): FlowExecutionImpl<I> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    infix fun timeout(timer: () -> String): FlowReactionImpl<I, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    infix fun timeout(timer: String): FlowReactionImpl<I, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

data class FlowReactionAction<M: FlowMessage>(val type: FlowActionType = FlowActionType.success, val id: String = "")

interface FlowReactionMessage<M: FlowMessage> {

    infix fun by(reaction: (M) -> Any)

}

class FlowReactionImpl<I: Any, M: FlowMessage>: FlowNode,
    FlowReactionMessage<M> {

    override var id = ""
    lateinit var listener: FlowMessagePattern<M>
    var reactionType: FlowReactionType = FlowReactionType.message
    var actionType: FlowActionType = FlowActionType.success
    var action: ((M) -> Any)? = null

    infix fun having(key: () -> Pair<String, Any>): FlowReactionImpl<I, M> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    infix fun supporting(assertion: (M) -> Boolean): FlowReactionImpl<I, M> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    infix fun create(action: FlowReactionAction<M>): FlowReactionMessage<M> {
        this.actionType = action.type
        this.id = action.id
        return this
    }

    override infix fun by(reaction: (M) -> Any) {
        this.action = reaction
    }

    infix fun mitigation(definition: FlowExecutionImpl<I>.() -> Unit): FlowExecutionImpl<I> = TODO()

}

