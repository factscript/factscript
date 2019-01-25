package io.factdriven.flow

import io.factdriven.flow.lang.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
inline fun <reified I: Aggregate> define(name: ElementName = I::class.simpleName!!, type: KClass<I> = I::class, definition: FlowExecution<I>.() -> Unit): FlowDefinition {

    return execute (name, type, definition).asDefinition()

}

inline fun <reified I: Aggregate> execute(name: ElementName = I::class.simpleName!!, type: KClass<I> = I::class, definition: FlowExecution<I>.() -> Unit): FlowExecution<I> {

    val flowExecution = FlowExecutionImpl<I>(null).apply(definition)
    flowExecution.name = name
    flowExecution.aggregateType = type
    return flowExecution

}
