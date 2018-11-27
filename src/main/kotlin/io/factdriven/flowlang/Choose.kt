package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Choose<A: Any> {

    infix fun one(options: Options<A>.() -> Unit): Options<A> = Options<A>().apply(options)
    infix fun many(options: Options<A>.() -> Unit): Options<A> = Options<A>().apply(options)

}

class Options<A: Any> {

    fun given(condition: () -> Boolean): Options<A> { return this }

    infix fun flow(flow: Flow<A>.() -> Unit): Flow<A> = Flow<A>().apply(flow)

}
