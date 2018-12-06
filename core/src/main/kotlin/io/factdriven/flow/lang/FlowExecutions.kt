package io.factdriven.flow.lang

import io.factdriven.flow.FlowInstance
import io.factdriven.flow.FlowMessage
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
    val select: FlowSelection<I>
    val create: FlowActionImpl<I, Any>

}

interface FlowDefinition<I : FlowInstance> {

    val nodes: List<FlowNode>
    val type: FlowDefinitionType

}

enum class FlowDefinitionType {

    execution, service, receive

}

class FlowExecutionImpl<I: FlowInstance>: FlowDefinition<I>,
    FlowExecution<I>, FlowActivities<I> {

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

    override infix fun service(service: FlowExecutionImpl<I>.() -> Unit): FlowExecutionImpl<I> {
        type = FlowDefinitionType.service
        this.apply(service)
        return this
    }

    override infix fun receive(receive: FlowExecutionImpl<I>.() -> Unit): FlowExecutionImpl<I> = TODO()

    override val on: FlowReactions<I>
        get() {
        return FlowReactions(this)
    }

    override val execute: FlowActivities<I>
        get() {
        val node = FlowExecutionImpl<I>()
        nodes.add(node)
        return node
    }

    fun execute(definition: FlowExecutionImpl<I>.() -> Unit): FlowExecution<I> = TODO()

    override val select: FlowSelection<I> get() = TODO()

    override val create: FlowActionImpl<I, Any>
        get() {
        val node = FlowActionImpl<I, Any>()
        nodes.add(node)
        return node
    }

    fun <M: FlowMessage> type(type: KClass<M>): FlowMessagePattern<M> {
        return FlowMessagePattern(type)
    }

    fun <M: FlowMessage> pattern(pattern: M): FlowMessagePattern<M> {
        TODO()
    }


    inline fun <reified M: FlowMessage> intent(kClass: KClass<*> = M::class): FlowReactionAction<M> {
        return intent(id(kClass))
    }

    inline fun <reified M: FlowMessage> intent(id: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.intent, id)
    }

    inline fun <reified M: FlowMessage> acceptance(kClass: KClass<*> = M::class): FlowReactionAction<M> {
        return acceptance(id(kClass))
    }

    inline fun <reified M: FlowMessage> acceptance(id: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.acceptance, id)
    }

    inline fun <reified M: FlowMessage> progress(kClass: KClass<*> = M::class): FlowReactionAction<M> {
        return progress(id(kClass))
    }

    inline fun <reified M: FlowMessage> progress(id: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.progress, id)
    }

    inline fun <reified M: FlowMessage> success(kClass: KClass<*> = M::class): FlowReactionAction<M> {
        return success(id(kClass))
    }

    inline fun <reified M: FlowMessage> success(id: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.success, id)
    }

    inline fun <reified M: FlowMessage> fix(kClass: KClass<*> = M::class): FlowReactionAction<M> {
        return fix(id(kClass))
    }

    inline fun <reified M: FlowMessage> fix(id: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.fix, id)
    }

    inline fun <reified M: FlowMessage> failure(kClass: KClass<*> = M::class): FlowReactionAction<M> {
        return failure(id(kClass))
    }

    inline fun <reified M: FlowMessage> failure(id: String): FlowReactionAction<M> {
        return FlowReactionAction(FlowActionType.failure, id)
    }
}

data class FlowMessagePattern<M: FlowMessage>(val type: KClass<out M>)

fun id(kClass: KClass<*>): String {
    return kClass.simpleName ?: ""
}
