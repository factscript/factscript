package io.factdriven.flow.lang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias FlowMessage = Any
typealias FlowMessages = List<FlowMessage>
typealias FlowMessagePatterns = List<FlowMessagePattern>

data class FlowMessagePattern(val type: KClass<out FlowMessage>, val keys: Map<String, Any?>)
