package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Throw<A: Any, M: Any> {

    infix fun having(correlation: (M) -> Pair<String, Any>): Throw<A, M> {
        return this
    }

    infix fun mitigate(flow: Flow<A>.() -> Unit): Flow<A> = Flow<A>().apply(flow)

    infix fun intent(id: (M) -> Any) {}
    infix fun progress(id: (M) -> Any) {}
    infix fun success(id: (M) -> Any) {}
    infix fun retry(id: (M) -> Any) {}
    infix fun failure(id: (M) -> Any) {}

}
