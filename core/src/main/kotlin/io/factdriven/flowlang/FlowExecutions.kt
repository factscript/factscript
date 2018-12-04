package io.factdriven.flowlang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

inline fun <reified I: FlowInstance> execute(name: String = label(I::class), definition: FlowExecutionImpl<I>.() -> Unit): FlowExecution<I> {
    val flowExecution = FlowExecutionImpl<I>().apply(definition)
    flowExecution.name = name
    return flowExecution
}

interface FlowNode {

    val name: String

}

interface FlowActivities<I: FlowInstance> {

    infix fun service(service: FlowExecutionImpl<I>.() -> Unit): FlowExecutionImpl<I>
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

    override var name: String = ""
        get() {
            return when (type) {
                FlowDefinitionType.service -> nodes[0].name
                else -> field
            }
        }

    override infix fun service(service: FlowExecutionImpl<I>.() -> Unit): FlowExecutionImpl<I>  {
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

    fun execute(definition: FlowExecutionImpl<I>.() -> Unit): FlowExecution<I> = TODO()

    override val select: Selection<I> get() = TODO()

    override val create: FlowActionImpl<I, Any> get() {
        val node = FlowActionImpl<I, Any>()
        nodes.add(node)
        return node
    }

    fun <M: Message> type(type: KClass<M>): FlowListener<M> {
        return FlowListener(type)
    }

    fun <M: Message> pattern(pattern: M): FlowListener<M> {
        TODO()
    }

    inline fun <reified M: Message> intent(name: String = label(M::class)): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.intent, name)
    }

    inline fun <reified M: Message> acceptance(name: String = label(M::class)): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.acceptance, name)
    }

    inline fun <reified M: Message> progress(name: String = label(M::class)): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.progress, name)
    }

    inline fun <reified M: Message> success(name: String = label(M::class)): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.success, name)
    }

    inline fun <reified M: Message> fix(name: String = label(M::class)): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.fix, name)
    }

    inline fun <reified M: Message> failure(name: String = label(M::class)): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.failure, name)
    }

}

data class FlowListener<M: Message>(val type: KClass<out M>)

fun label(kClass: KClass<*>): String {
    val regex = String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])").toRegex()
    val className = kClass.simpleName ?: ""
    val splitted = className.split(regex)
    val joined = splitted[0] + if (splitted.size > 1) splitted.subList(1, splitted.size).joinToString(separator = "") { " " + it.substring(0, 1).toLowerCase() + it.substring(1) } else ""
    return joined
}
