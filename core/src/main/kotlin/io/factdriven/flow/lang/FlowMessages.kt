package io.factdriven.flow.lang

import java.security.MessageDigest
import java.math.BigInteger



/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias FlowMessageId = String
typealias FlowMessageTarget = Pair<FlowElementType, AggregateId>
typealias FlowMessageType = String
typealias FlowMessagePayload = Any
typealias FlowMessageProperty = String
typealias FlowMessagePropertyValue = Any?
typealias FlowMessages = List<FlowMessagePayload>
typealias FlowMessagePatterns = List<FlowMessagePattern>

data class FlowMessage(
    val id: FlowMessageId,
    val payload: FlowMessagePayload,
    val target: FlowMessageTarget? = null) {

    constructor(message: FlowMessage, target: FlowMessageTarget): this(message.id, message.payload, target)

}

data class FlowMessagePattern(
    val type: FlowMessageType,
    val keys: Map<FlowMessageProperty, FlowMessagePropertyValue>? = null) {

    val hash: String

    init {
        val buffer = StringBuffer(type)
        keys?.toSortedMap()?.forEach {
            buffer.append("|").append(it.key).append("=").append(it.value)
        }
        hash = hash(buffer.toString())
    }

    companion object {

        val digest = MessageDigest.getInstance("MD5")

        private fun hash(input: String): String {
            val bytes = digest.digest(input.toByteArray())
            return String.format("%0" + (bytes.size shl 1) + "x", BigInteger(1, bytes))
        }

    }

}