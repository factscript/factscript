package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
open class Action<S: Instance, M: Any> {

    infix fun mitigation(definition: Definition<S>.() -> Unit): Definition<S> = TODO()

    infix fun intent(id: (M) -> Any) {
        TODO()
    }

    infix fun milestone(id: (M) -> Any) {
        TODO()
    }

    infix fun success(id: (M) -> Any) {
        TODO()
    }

    infix fun retry(id: (M) -> Any) {
        TODO()
    }

    infix fun failure(id: (M) -> Any) {
        TODO()
    }

}

class Reaction<A: Any, M: Any>: Action<A, M>() {

    infix fun having(correlation: () -> Pair<String, Any>): Reaction<A, M> {
        TODO()
    }

    infix fun supporting(assertion: (M) -> Boolean): Reaction<A, M> {
        TODO()
    }

}
