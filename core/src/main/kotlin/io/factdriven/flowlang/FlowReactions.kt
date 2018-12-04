package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class FlowReactions<I: Any>(val parent: FlowExecutionImpl<I>) {

    infix fun <M: Any> message(listener: FlowListener<M>): FlowReaction<I, M> {
        val reaction = FlowReaction<I, M>()
        parent.nodes.add(reaction)
        reaction.listener = listener
        return reaction
    }

    infix fun compensation(definition: FlowExecutionImpl<I>.() -> Unit): FlowExecutionImpl<I> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    infix fun timeout(timer: () -> String): FlowReaction<I, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    infix fun timeout(timer: String): FlowReaction<I, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

interface FlowReactionActions {

    infix fun <M: Message> acceptance(action: (M) -> Any)
    infix fun <M: Message> progress(action: (M) -> Any)
    infix fun <M: Message> success(action: (M) -> Any)
    infix fun <M: Message> rerun(action: (M) -> Any)
    infix fun <M: Message> failure(action: (M) -> Any)

}

class FlowReaction<I: Any, M: Message>: FlowNode {

    lateinit var listener: FlowListener<M>

    override val label: String get() {
        return listener.type.java.simpleName
    }

    infix fun having(key: () -> Pair<String, Any>): FlowReaction<I, M> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    infix fun supporting(assertion: (M) -> Boolean): FlowReaction<I, M> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var actionType: FlowActionType = FlowActionType.success
    var action: ((M) -> Any)? = null

    infix fun acceptance(action: (M) -> Any) {
        TODO()
    }

    infix fun progress(action: (M) -> Any) {
        TODO()
    }

    infix fun success(action: (M) -> Any) {
        actionType = FlowActionType.success
        this.action = action
    }

    infix fun rerun(action: (M) -> Any) {
        TODO()
    }

    infix fun failure(action: (M) -> Any) {
        TODO()
    }

    infix fun mitigation(definition: FlowExecutionImpl<I>.() -> Unit): FlowExecutionImpl<I> = TODO()

}

