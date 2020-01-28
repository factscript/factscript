package io.factdriven.play

import com.fasterxml.jackson.annotation.JsonIgnore
import io.factdriven.def.Catching
import io.factdriven.def.Definition
import io.factdriven.def.Node
import io.factdriven.flow.lang.getValue
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.reflect.KClass
import kotlin.collections.Map

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Handler (val handlerId: HandlerId, val handling: Handling)

data class HandlerId (val type: String, val id: String?) {
    constructor(type: KClass<*>, id: String? = null): this(type.simpleName!!, id)
}

data class Handling (val fact: String, val details: Map<String, Any?> = emptyMap()) {

    constructor(fact: KClass<*>, details: Map<String, Any?> = emptyMap()): this (fact.simpleName!!, details)

    val hash = hash(this) @JsonIgnore get

    companion object {

        private val digest = MessageDigest.getInstance("MD5")

        private fun hash(handling: Handling): String {
            val buffer = StringBuffer(handling.fact)
            handling.details.toSortedMap().forEach {
                buffer.append("|").append(it.key).append("=").append(it.value)
            }
            val bytes = digest.digest(buffer.toString().toByteArray())
            return String.format("%0" + (bytes.size shl 1) + "x", BigInteger(1, bytes))
        }

    }

}

fun Catching.handling(message: Message): Handling? {
    return if (catchingType.isInstance(message.fact.details))
        Handling(catchingType, catchingProperties.map { it to message.fact.details.getValue(it) }.toMap())
    else null
}

fun Definition.handling(message: Message): List<Handling> {
    fun handling(node: Node): List<Handling> {
        return when(node) {
            is Catching -> node.handling(message)?.let { listOf(it) } ?: emptyList()
            is Definition -> children.map { handling(it) }.flatten()
            else -> emptyList()
        }
    }
    return handling(this)
}

fun Definition.Companion.handling(message: Message): List<Handling> {
    return all.values.map {
        it.handling(message)
    }.flatten()
}
