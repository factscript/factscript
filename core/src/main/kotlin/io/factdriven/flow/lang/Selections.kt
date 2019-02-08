package io.factdriven.flow.lang

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class SelectOptions<I: Entity> {

    infix fun one(option: FlowOption<I>.() -> Unit): FlowOption<I> = TODO()
    infix fun many(option: FlowOption<I>.() -> Unit): FlowOption<I> = TODO()

}

class FlowOption<I: Entity> {

    fun topic(name: String = ""): FlowOption<I> { TODO() }

    fun given(name: String = "", condition: I.() -> Boolean): FlowOption<I> { TODO() }

    infix fun execute(definition: UnclassifiedFlow<I>.() -> Unit): UnclassifiedFlow<I> = TODO()

    infix fun execute(definition: FlowOptions<I>): UnclassifiedFlow<I> = TODO()

}
