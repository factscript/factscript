package io.factdriven.impl.execution

import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.reflect.KClass
import kotlin.collections.Map

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Receptor(val receiving: Type, val expecting: Map<String, Any?> = emptyMap(), val correlating: MessageId? = null) {

    constructor(receiving: KClass<*>, expecting: Map<String, Any?> = emptyMap()): this (receiving.type, expecting)
    constructor(receiving: KClass<*>, correlating: MessageId): this (receiving.type, emptyMap(), correlating)

    val hash = hash(this) @JsonIgnore get

    companion object {

        private val digest = MessageDigest.getInstance("MD5")

        private fun hash(receptor: Receptor): String {
            val buffer = StringBuffer(receptor.receiving.context)
            buffer.append("|").append(receptor.receiving.name)
            if (receptor.correlating != null) {
                buffer.append("|correlating=").append(receptor.correlating.hash)
            } else {
                receptor.expecting.toSortedMap().forEach {
                    buffer.append("|").append(it.key).append("=").append(it.value)
                }
            }
            val bytes = digest.digest(buffer.toString().toByteArray())
            return String.format("%0" + (bytes.size shl 1) + "x", BigInteger(1, bytes))
        }

    }

}

data class Receiver (val entity: EntityId, val receptor: Receptor)
