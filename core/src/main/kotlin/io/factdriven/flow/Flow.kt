package io.factdriven.flow

import io.factdriven.flow.lang.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
inline fun <reified I: FlowInstance> define(name: FlowElementName = I::class.simpleName!!, type: KClass<I> = I::class, definition: FlowExecution<I>.() -> Unit): FlowDefinition {

    return execute (name, type, definition).asDefinition()

}

inline fun <reified I: FlowInstance> execute(name: FlowElementName = I::class.simpleName!!, type: KClass<I> = I::class, definition: FlowExecution<I>.() -> Unit): FlowExecution<I> {

    val flowExecution = FlowExecutionImpl<I>(null).apply(definition)
    flowExecution.name = name
    flowExecution.instanceType = type
    return flowExecution

}
