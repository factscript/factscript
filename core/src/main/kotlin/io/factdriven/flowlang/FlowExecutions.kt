package io.factdriven.flowlang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

inline fun <reified I: FlowInstance> execute(id: String = id(I::class), definition: FlowExecutionImpl<I>.() -> Unit): FlowExecution<I> {
    val flowExecution = FlowExecutionImpl<I>().apply(definition)
    flowExecution.id = id
    return flowExecution
}

interface FlowNode {

    val id: String

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

    override var id: String = ""
        get() {
            return when (type) {
                FlowDefinitionType.service -> nodes[0].id
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

    inline fun <reified M: Message> intent(id: String = id(M::class)): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.intent, id)
    }

    inline fun <reified M: Message> acceptance(id: String = id(M::class)): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.acceptance, id)
    }

    inline fun <reified M: Message> progress(id: String = id(M::class)): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.progress, id)
    }

    inline fun <reified M: Message> success(id: String = id(M::class)): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.success, id)
    }

    inline fun <reified M: Message> fix(id: String = id(M::class)): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.fix, id)
    }

    inline fun <reified M: Message> failure(id: String = id(M::class)): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.failure, id)
    }

}

data class FlowListener<M: Message>(val type: KClass<out M>)

fun id(kClass: KClass<*>): String {
    return kClass.simpleName ?: ""
}

fun name(kClass: KClass<*>): String {
    val regex = String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])").toRegex()
    val cName = kClass.simpleName ?: ""
    val split = cName.split(regex)
    return split[0] + if (split.size > 1) split.subList(1, split.size).joinToString(separator = "") { " " + it.substring(0, 1).toLowerCase() + it.substring(1) } else ""
}
