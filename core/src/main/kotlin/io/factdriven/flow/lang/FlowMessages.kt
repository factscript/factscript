package io.factdriven.flow.lang

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias FlowMessage = Any
typealias FlowMessages = List<FlowMessage>

interface FlowMessagePattern<M: FlowMessage> {

    val type: KClass<out M>

    fun matches(message: FlowMessage): Boolean

}

typealias FlowMessagePatterns = List<FlowMessagePattern<out FlowMessage>>

data class DefaultFlowMessagePattern<M: FlowMessage>(override val type: KClass<M>) : FlowMessagePattern<M> {

    internal val keys = mutableMapOf<String, Any>()

    internal fun add(key: Pair<String, Any>) {
        keys[key.first] = key.second
    }

    override fun matches(message: FlowMessage): Boolean {
        return type.isInstance(message) && keys.all { property ->
            val memberProperty = message.javaClass.kotlin.memberProperties.find { it.name == property.key }
            memberProperty != null && memberProperty.get(message) == property.value
        }
    }

}
