package io.factdriven.flow

import io.factdriven.flow.lang.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
inline fun <reified I: Entity> define(name: ElementName = I::class.simpleName!!, type: KClass<I> = I::class, definition: FlowExecution<I>.() -> Unit): FlowDefinition<I> {

    return execute (name, type, definition).asDefinition()

}

inline fun <reified I: Entity> execute(name: ElementName = I::class.simpleName!!, type: KClass<I> = I::class, definition: FlowExecution<I>.() -> Unit): FlowExecution<I> {

    val flowExecution = FlowExecutionImpl<I>(null).apply(definition)
    flowExecution.name = name
    flowExecution.aggregateType = type
    FlowDefinitions.add(flowExecution)
    return flowExecution

}
