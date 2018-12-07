package io.factdriven.flow

import io.factdriven.flow.lang.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
inline fun <reified I: FlowInstance> execute(name: FlowDefinitionId = I::class.simpleName!!, type: KClass<I> = I::class, definition: FlowExecution<I>.() -> Unit): FlowExecutionDefinition {

    val flowExecution = FlowExecutionImpl<I>().apply(definition)
    flowExecution.name = name
    flowExecution.instanceType = type
    return flowExecution

}
