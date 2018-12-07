package io.factdriven.flow.lang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias FlowMessage = Any
typealias FlowMessages = List<FlowMessage>

interface FlowMessagePattern<M: FlowMessage> { val type: KClass<out M> }
typealias FlowMessagePatterns = List<FlowMessagePattern<out FlowMessage>>

data class DefaultFlowMessagePattern<M: FlowMessage>(override val type: KClass<out M>) : FlowMessagePattern<M>
