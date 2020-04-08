package io.factdriven.execution

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.math.BigInteger
import java.security.MessageDigest
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

    companion object {

        private val mapper = jacksonObjectMapper()

        fun from(type: KClass<*>, fact: Fact<*>): Message {
            val messageId = MessageId(StreamId(type, fact.id))
            return Message(messageId, fact)
        }

        fun from(history: List<Message>, fact: Fact<*>, correlating: MessageId? = null): Message {
            val messageId = MessageId.nextId(history.last().id)
            return Message(messageId, fact, null, correlating)
        }

        fun handle(message: Message, handler: Handler): Message {
            return Message(message.id, message.fact, handler, message.correlating)
        }

        fun fromJson(json: String): Message {
            return fromJson(mapper.readTree(json))
        }

        internal fun fromJson(tree: JsonNode): Message {
            val id = mapper.readValue(mapper.treeAsTokens(tree.get("id")), MessageId::class.java)
            val handler = mapper.readValue(mapper.treeAsTokens(tree.get("handler")), Handler::class.java)
            val fact = Fact.fromJson(tree.path("fact"))
            val correlating = mapper.readValue(mapper.treeAsTokens(tree.get("correlating")), MessageId::class.java)
            return Message(id, fact, handler, correlating)
        }

    }

    object list {

        fun fromJson(json: String): List<Message> {
            return fromJson(mapper.readTree(json))
        }

        fun fromJson(tree: JsonNode): List<Message> {
            return if (tree.isArray) tree.map { Message.fromJson(it) } else listOf(Message.fromJson(tree))
        }

    }

}

data class MessageId(val streamId: StreamId, val version: Int = 0) {

    val hash = hash(this) @JsonIgnore get

    companion object {

        private val digest = MessageDigest.getInstance("MD5")

        fun nextId(after: MessageId): MessageId {
            return MessageId(after.streamId, after.version + 1)
        }

        private fun hash(messageId: MessageId): String {
            val buffer = StringBuffer()
            buffer.append("name=").append(messageId.streamId.name)
            if (messageId.streamId.id != null) {
                buffer.append("|id=").append(messageId.streamId.id)
            }
            buffer.append("|version=").append(messageId.version)
            val bytes = digest.digest(buffer.toString().toByteArray())
            return String.format("%0" + (bytes.size shl 1) + "x", BigInteger(1, bytes))
        }

    }

}

val Any.json: String get() {
    return jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)
}

/*
 * Unique, but human manageable and readable name for object types
 */
data class Type(val context: String, /* Name unique within given context */ val local: String) {

    companion object {

        fun from(kClass: KClass<*>): Type {
            val name = kClass.java.name
            val index = name.lastIndexOf('.')
            return Type(name.substring(0, index), name.substring(index + 1))
        }

        fun from(string: String): Type {
            return from(Class.forName(string).kotlin)
        }

    }

    override fun toString(): String {
        return "$context.$local"
    }

    fun toKClass(): KClass<*> {
        return Class.forName(toString()).kotlin
    }

    fun toLabel(): String {
        return local.let { local -> local.replace("(.)([A-Z\\d])".toRegex()) { "${it.groupValues[1]} ${it.groupValues[2].toLowerCase()}" } }
    }

}

val KClass<*>.type: Type get() = Type.from(this)

fun <A: Any> List<Message>.applyTo(type: KClass<A>): A {
    return this.map { it.fact }.applyTo(type)
}
