package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class FlowReactions<I: Any>(val parent: FlowExecutionImpl<I>) {

    infix fun <M: Any> message(listener: FlowListener<M>): FlowReactionImpl<I, M> {
        val reaction = FlowReactionImpl<I, M>()
        parent.nodes.add(reaction)
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

data class FlowReactionAction<M: Message>(val type: FlowActionType = FlowActionType.success, val name: String = "")

interface FlowReactionMessage<M: Message> {

    infix fun by(reaction: (M) -> Any)

}

class FlowReactionImpl<I: Any, M: Message>: FlowNode, FlowReactionMessage<M> {

    override var name = ""
    lateinit var listener: FlowListener<M>
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
        this.name = action.name
        return this
    }

    override infix fun by(reaction: (M) -> Any) {
        this.action = reaction
    }

    infix fun mitigation(definition: FlowExecutionImpl<I>.() -> Unit): FlowExecutionImpl<I> = TODO()

}

