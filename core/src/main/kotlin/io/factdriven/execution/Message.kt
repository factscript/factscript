package io.factdriven.execution

import io.factdriven.implementation.utils.Json
import kotlin.reflect.KClass


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Message (

    val id: MessageId,
    val fact: Fact<*>,
    val handler: Handler? = null,
    val correlating: MessageId? = null

) {

    constructor(type: KClass<*>, fact: Fact<*>): this(MessageId(EntityId(type, fact.id)), fact)
    constructor(history: List<Message>, fact: Fact<*>, correlating: MessageId? = null): this(MessageId.nextAfter(history.last().id), fact, null, correlating)
    constructor(message: Message, handler: Handler): this(message.id, message.fact, handler, message.correlating)

    companion object {

        fun fromJson(json: String): Message {
            return fromJson(Json(json))
        }

        fun fromJson(json: Json): Message {
            return Message(
                json.getObject("id")!!,
                Fact.fromJson(json.getNode("fact")),
                json.getObject("handler"),
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