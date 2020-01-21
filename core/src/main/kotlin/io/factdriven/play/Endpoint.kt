package io.factdriven.play

import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.reflect.KClass
import kotlin.collections.Map

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

data class Endpoint (val handler: Handler, val handling: Handling)

data class Handler (val context: String?, val type: String, val id: String?) {
    constructor(type: String, id: String? = null): this(null, type, id)
    constructor(type: KClass<*>, id: String? = null): this(null, type.simpleName!!, id)
}

data class Handling (val fact: String, val details: Map<String, Any> = emptyMap()) {

    constructor(fact: KClass<*>, details: Map<String, Any> = emptyMap()): this (fact.simpleName!!, details)

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
