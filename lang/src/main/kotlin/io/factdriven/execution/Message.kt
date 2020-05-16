package io.factdriven.execution

import com.fasterxml.jackson.annotation.JsonIgnore
import io.factdriven.language.Flows.all
import io.factdriven.language.definition.*
import io.factdriven.language.impl.definition.*
import io.factdriven.language.impl.utils.Id
import io.factdriven.language.impl.utils.Json
import org.slf4j.*
import kotlin.math.*
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

    // INSTANTIATE
    constructor(type: KClass<*>, fact: Fact<*>, correlating: MessageId? = null): this(MessageId(EntityId(type, fact.id)), fact, null, correlating)

    // SEND & RECEIVE
    constructor(history: List<Message>, fact: Fact<*>, correlating: MessageId? = null): this(MessageId.nextAfter(history.last().id), fact, null, correlating)

    // ROUTE
    constructor(message: Message, receiver: Receiver): this(message.id, message.fact, receiver, message.correlating)

    companion object {

        private val log: Logger = LoggerFactory.getLogger(Message::class.java)

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

    fun log(operation: String) {
        val factPad = " ".repeat(max(fact.type.name.length, all().map { it.descendants }.flatten().filter { it is ThrowingImpl<*,*> || it is CorrelatingImpl<*> }.map { if (it is Consuming) it.consuming.type.name.length else (it as Throwing).throwing.type.name.length }.max()!!)).substring(fact.type.name.length)
        val entityPad = " ".repeat(max(id.entity.type.name.length, all().map{ it.entity.type.name.length }.max()!!)).substring(id.entity.type.name.length)
        val operationPad = if (operation == "PUBLISH") "--- ${" ".repeat(7).substring(operation.length)}$operation -->" else "<-- ${" ".repeat(7).substring(operation.length)}$operation ---"
        log.debug(
            "${entityPad}${id.entity.type.name}(${id.entity.id?.split("-")?.get(1) ?: ""})[${id.version}]" +
            " $operationPad" +
            " ${factPad}${fact.type.name} (${id.hash.split("-")[1]}" +
            (correlating?.let { ":${it.hash.split("-")[1]})" } ?: ":" + "-".repeat(8) + ")") +
            (receiver?.let { " -> ${it.entity.type.name}(${it.entity.id?.split("-")?.get(1) ?: ""})" } ?: "")
         )
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