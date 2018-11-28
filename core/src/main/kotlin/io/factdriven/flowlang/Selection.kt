package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Selection<A: Any> {

    infix fun one(option: Option<A>.() -> Unit): Option<A> = TODO()
    infix fun many(option: Option<A>.() -> Unit): Option<A> = TODO()

}

class Option<A: Any> {

    fun given(condition: () -> Boolean): Option<A> { TODO() }

    infix fun execute(definition: Definition<A>.() -> Unit): Definition<A> = TODO()

}
