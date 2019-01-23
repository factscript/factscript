package io.factdriven.flow.lang

import java.security.MessageDigest
import java.math.BigInteger
import kotlin.reflect.KClass


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias Message = Any
typealias MessageType = KClass<out Message>
typealias MessageId = String
typealias MessageTarget = Pair<AggregateType, AggregateId>

typealias PropertyName = String
typealias PropertyValue = Any?

typealias Messages = List<Message>
typealias MessagePatterns = List<MessagePattern>

data class MessageContainer(

    val id: MessageId,
    val message: Message,
    val target: MessageTarget? = null

) {

    constructor(container: MessageContainer, target: MessageTarget): this(container.id, container.message, target)

}

data class MessagePattern(

    val type: MessageType,
    val properties: Map<PropertyName, PropertyValue>? = null

) {

    val hash: String

    init {
        val buffer = StringBuffer(type.simpleName) // TODO simple name is just fallback
        properties?.toSortedMap()?.forEach {
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