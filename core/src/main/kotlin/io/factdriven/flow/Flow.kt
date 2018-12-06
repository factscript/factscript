package io.factdriven.flow

import io.factdriven.flow.lang.FlowExecutionDefinition
import io.factdriven.flow.lang.FlowExecution
import io.factdriven.flow.lang.FlowExecutionImpl
import io.factdriven.flow.lang.FlowMessagePattern
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

typealias FlowMessage = Any
typealias FlowMessages = List<FlowMessage>
typealias FlowMessagePatterns = List<FlowMessagePattern<out FlowMessage>>
typealias FlowInstance = Any
typealias FlowInstanceId = String
typealias FlowDefinitionId = String
typealias FlowInstanceIds = List<FlowInstanceId>

inline fun <reified I: FlowInstance> execute(name: FlowDefinitionId = I::class.simpleName!!, type: KClass<I> = I::class, definition: FlowExecution<I>.() -> Unit): FlowExecutionDefinition {

    val flowExecution = FlowExecutionImpl<I>().apply(definition)
    flowExecution.name = name
    flowExecution.instanceType = type
    return flowExecution

}

/**
 * Reconstruct the past flow instance state based on a given history of messages.
 * @param history of (consumed and produced) messages
 * @param flow definition
 * @return instance summarizing the state of a specific flow
 */
fun <I: FlowInstance> past(history: FlowMessages, flow: FlowExecution<I>): I {
    val type = flow.asDefinition().instanceType
    val constructor = type.constructors.find { it.parameters.size == 1 && it.parameters[0].type.classifier as KClass<*> == type }
    TODO()
}

/**
 * Produce new "present" messages based on a given history of messages and a trigger.
 * @param history of (consumed and produced) messages
 * @param flow definition
 * @param trigger coming in and influencing the flow instance
 * @return new messages produced
 */
fun <I: FlowInstance> present(history: FlowMessages, flow: FlowExecution<I>, trigger: FlowMessage): FlowMessages {
    TODO()
}

/**
 * Produce a list of future matching patterns matching in the future based on a given history of messages and a trigger.
 * @param history of (consumed and produced) messages
 * @param flow definition
 * @param trigger coming in and influencing the flow instance
 * @return future matching patterns
 */
fun <I: FlowInstance> future(history: FlowMessages, flow: FlowExecution<I>, trigger: FlowMessage): FlowMessagePatterns {
    TODO()
}

/**
 * Produce a list of potential patterns for an incoming message.
 * @param flow definition
 * @param trigger coming in and potentially influencing many flow instances
 * @return matching patterns
 */
fun <I: FlowInstance> potential(flow: FlowExecution<I>, trigger: FlowMessage): FlowMessagePatterns {
    TODO()
}

/**
 * Determine a list of flow instances currently matching to given patterns.
 * @param patterns
 * @return matching flow instances
 */
fun <I: FlowInstance> determine(patterns: FlowMessagePatterns): FlowInstanceIds {
    TODO()
}
