package io.factdriven.flowlang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

fun <I: FlowInstance> execute(definition: FlowExecutionImpl<I>.() -> Unit): FlowExecution<I> = FlowExecutionImpl<I>().apply(definition)

interface FlowNode {

    val label: String

}

interface FlowActivities<I: FlowInstance> {

    infix fun service(service: FlowExecutionImpl<I>.() -> Unit): FlowActivities<I>
    infix fun receive(receive: FlowExecutionImpl<I>.() -> Unit): FlowExecutionImpl<I>

}


interface FlowExecution<I : FlowInstance>: FlowNode {

    val on: FlowReactions<I>
    val execute: FlowActivities<I>
    val select: Selection<I>
    val create: FlowActionImpl<I, Any>

}

interface FlowDefinition<I : FlowInstance> {

    val nodes: List<FlowNode>
    val type: FlowDefinitionType

}

enum class FlowDefinitionType {

    execution, service, receive

}

class FlowExecutionImpl<I: FlowInstance>: FlowDefinition<I>, FlowExecution<I>, FlowActivities<I> {

    lateinit var status: I

    override var type = FlowDefinitionType.execution

    override val nodes = mutableListOf<FlowNode>()

    private var labeled: String? = null

    override val label: String get() {
        return labeled ?: ""
    }

    override infix fun service(service: FlowExecutionImpl<I>.() -> Unit): FlowActivities<I>  {
        type = FlowDefinitionType.service
        this.apply(service)
        return this
    }

    override infix fun receive(receive: FlowExecutionImpl<I>.() -> Unit): FlowExecutionImpl<I> = TODO()

    override val on: FlowReactions<I> get() {
        return FlowReactions(this)
    }

    override val execute: FlowActivities<I> get() {
        val node = FlowExecutionImpl<I>()
        nodes.add(node)
        return node
    }

    override val select: Selection<I> get() = TODO()

    override val create: FlowActionImpl<I, Any> get() {
        val node = FlowActionImpl<I, Any>()
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
        this.labeled = label
    }

}

data class FlowListener<M: Message>(val type: KClass<out M>)

