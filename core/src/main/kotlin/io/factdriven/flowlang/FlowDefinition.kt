package io.factdriven.flowlang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

fun <I: FlowInstance> execute(definition: FlowDefinition<I>.() -> Unit): FlowDefinition<I> = FlowDefinition<I>().apply(definition)

interface FlowNode

class FlowDefinition<I: FlowInstance>: FlowNode {

    lateinit var status: I
    val nodes = mutableListOf<FlowNode>()

    val on: FlowTrigger<I> get() {
        val node = FlowTrigger<I>()
        nodes.add(node)
        return node
    }

    val execute: FlowExecution<I> get() {
        val node = FlowExecution<I>()
        nodes.add(node)
        return node
    }

    val select: Selection<I> get() = TODO()

    val create: FlowAction<I, Any> get() {
        val node = FlowAction<I, Any>()
        nodes.add(node)
        return node
    }

    infix fun <M: Message> type(type: KClass<M>): FlowListener<M> {
        return FlowListener(type)
    }

    infix fun <M: Message> pattern(pattern: M): FlowListener<M> {
        TODO()
    }

    infix fun labeled(label: String) {
        TODO()
    }

}

data class FlowListener<M: Message>(val type: KClass<out M>)
