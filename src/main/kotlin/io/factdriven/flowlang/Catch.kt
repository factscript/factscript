package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Catch<A: Any> {

    infix fun <M: Any> message(type: () -> Listener<M>): Throw<A, M> { return Throw() }
    infix fun timeout(timer: () -> String): Throw<A, Any> { return Throw() }
    infix fun timeout(timer: String): Throw<A, Any> { return Throw() }
    infix fun compensation(flow: Flow<A>.() -> Unit): Flow<A> = Flow<A>().apply(flow)

}
