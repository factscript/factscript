package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class FlowTrigger<A: Any>: FlowNode {

    lateinit var reaction: FlowReaction<A, *>

    infix fun <M: Any> message(listener: FlowListener<M>): FlowReactionToMessage<A, M> {
        val r = FlowReactionToMessage<A, M>(listener)
        reaction = r
        return r
    }

    infix fun timeout(timer: () -> String): FlowReaction<A, Any> { TODO() }
    infix fun timeout(timer: String): FlowReaction<A, Any> { TODO() }
    infix fun compensation(definition: FlowDefinition<A>.() -> Unit): FlowDefinition<A> = TODO()

}
