package io.factdriven.execution

import com.fasterxml.jackson.annotation.JsonIgnore
import io.factdriven.Flows
import io.factdriven.definition.api.Consuming
import io.factdriven.definition.api.Executing
import io.factdriven.definition.api.Node
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.reflect.KClass
import kotlin.collections.Map

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Handler (val stream: StreamId, val handling: Handling)

data class Handling (val fact: Type, val details: Map<String, Any?> = emptyMap(), val correlating: MessageId? = null) {

    constructor(fact: KClass<*>, details: Map<String, Any?> = emptyMap()): this (fact.type, details)
    constructor(fact: KClass<*>, correlating: MessageId): this (fact.type, emptyMap(), correlating)

    val hash = hash(this) @JsonIgnore get

    companion object {

        private val digest = MessageDigest.getInstance("MD5")

        private fun hash(handling: Handling): String {
            val buffer = StringBuffer(handling.fact.context)
            buffer.append("|").append(handling.fact.local)
            if (handling.correlating != null) {
                buffer.append("|correlating=").append(handling.correlating.hash)
            } else {
                handling.details.toSortedMap().forEach {
                    buffer.append("|").append(it.key).append("=").append(it.value)
                }
            }
            val bytes = digest.digest(buffer.toString().toByteArray())
            return String.format("%0" + (bytes.size shl 1) + "x", BigInteger(1, bytes))
        }

    }

}


fun Flows.handling(message: Message): List<Handling> {
    return Flows.all().map { it.handling(message) }.flatten()
}

fun Executing.handling(message: Message): Handling? {
    return if (catching.isInstance(message.fact.details) && message.correlating != null)
        Handling(catching, message.correlating)
    else null
}

fun Consuming.handling(message: Message): Handling? {
    return if (catching.isInstance(message.fact.details))
        Handling(catching, properties.map { it to message.fact.details.getValue(it) }.toMap())
    else null
}

fun Node.handling(message: Message): List<Handling> {
    fun handling(node: Node): List<Handling> {
        return when(node) {
            is Consuming -> node.handling(message)?.let { listOf(it) } ?: emptyList()
            is Executing -> node.handling(message)?.let { listOf(it) } ?: emptyList()
            else -> node.children.map { handling(it) }.flatten()
        }
    }
    return handling(this)
}

