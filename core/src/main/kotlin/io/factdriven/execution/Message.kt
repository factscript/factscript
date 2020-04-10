package io.factdriven.execution

import com.fasterxml.jackson.annotation.JsonIgnore
import io.factdriven.implementation.utils.Json
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.reflect.KClass


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Message (

    val id: MessageId,
    val fact: Fact<*>,
    val receiver: Receiver? = null,
    val correlating: MessageId? = null

) {

    constructor(type: KClass<*>, fact: Fact<*>): this(MessageId(EntityId(type, fact.id)), fact)
    constructor(history: List<Message>, fact: Fact<*>, correlating: MessageId? = null): this(MessageId.nextAfter(history.last().id), fact, null, correlating)
    constructor(message: Message, receiver: Receiver): this(message.id, message.fact, receiver, message.correlating)

    companion object {

        fun fromJson(json: String): Message {
            return fromJson(Json(json))
        }

        fun fromJson(json: Json): Message {
            return Message(
                json.getObject("id")!!,
                Fact.fromJson(json.getNode("fact")),
                json.getObject("receiver"),
                json.getObject("correlating")
            )
        }

    }

}

object Messages {

    fun fromJson(json: Json): List<Message> {
        return json.asList().map { Message.fromJson(it) }
    }

    fun fromJson(json: String): List<Message> {
        return fromJson(Json(json))
    }

}

data class MessageId(val entity: EntityId, val version: Int = 0) {

    val hash = hash(this) @JsonIgnore get

    companion object {

        fun nextAfter(last: MessageId): MessageId {
            return MessageId(last.entity, last.version + 1)
        }

        private val digest = MessageDigest.getInstance("MD5")

        private fun hash(messageId: MessageId): String {
            val buffer = StringBuffer()
            buffer.append("type=").append(messageId.entity.type)
            if (messageId.entity.id != null) {
                buffer.append("|id=").append(messageId.entity.id)
            }
            buffer.append("|version=").append(messageId.version)
            val bytes = digest.digest(buffer.toString().toByteArray())
            return String.format("%0" + (bytes.size shl 1) + "x", BigInteger(1, bytes))
        }

    }

}