package io.factdriven.flow.lang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class FlowSelection<A: Any> {

    infix fun one(option: FlowOption<A>.() -> Unit): FlowOption<A> = TODO()
    infix fun many(option: FlowOption<A>.() -> Unit): FlowOption<A> = TODO()

}

class FlowOption<A: Any> {

    fun topic(name: String = ""): FlowOption<A> { TODO() }

    fun given(name: String = "", condition: () -> Boolean): FlowOption<A> { TODO() }

    infix fun execute(definition: FlowExecutionImpl<A>.() -> Unit): FlowExecutionImpl<A> = TODO()

    infix fun execute(definition: FlowActivities<A>): FlowExecutionImpl<A> = TODO()

}
