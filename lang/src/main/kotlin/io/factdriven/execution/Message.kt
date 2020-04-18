package io.factdriven.execution

import com.fasterxml.jackson.annotation.JsonIgnore
import io.factdriven.impl.utils.Id
import io.factdriven.impl.utils.Json
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

    constructor(type: KClass<*>, fact: Fact<*>): this(
        MessageId(
            EntityId(
                type,
                fact.id
            )
        ), fact)
    constructor(history: List<Message>, fact: Fact<*>, correlating: MessageId? = null): this(
        MessageId.nextAfter(history.last().id), fact, null, correlating)
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

data class MessageId(val entity: EntityId, val version: Int = 0) {

    val hash = Id(this) @JsonIgnore get

    companion object {

        fun nextAfter(last: MessageId): MessageId {
            return MessageId(last.entity, last.version + 1)
        }

    }

}

interface MessageProcessor {
    fun process(message: Message)
}

interface MessagePublisher {
    fun publish(vararg message: Message)
}

interface MessageStore {
    fun load(id: String): List<Message>
}