package io.factdriven.flowlang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Selection<A: Any> {

    infix fun one(option: Option<A>.() -> Unit): Option<A> = TODO()
    infix fun many(option: Option<A>.() -> Unit): Option<A> = TODO()

}

interface AnsweringOption<A: Any>

class Option<A: Any>: AnsweringOption<A> {

    fun topic(name: String = ""): Option<A> { TODO() }

    fun given(name: String = "", condition: () -> Boolean): Option<A> { TODO() }

    infix fun execute(definition: FlowExecutionImpl<A>.() -> Unit): FlowExecutionImpl<A> = TODO()

    infix fun execute(definition: FlowActivities<A>): FlowExecutionImpl<A> = TODO()

}
