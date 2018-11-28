package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Trigger<A: Any> {

    infix fun <M: Any> message(type: () -> Listener<M>): Reaction<A, M> { TODO() }
    infix fun timeout(timer: () -> String): Reaction<A, Any> { TODO() }
    infix fun timeout(timer: String): Reaction<A, Any> { TODO() }
    infix fun compensation(definition: Definition<A>.() -> Unit): Definition<A> = TODO()

}
