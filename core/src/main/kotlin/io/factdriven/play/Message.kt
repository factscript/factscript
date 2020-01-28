package io.factdriven.play

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*
import kotlin.reflect.KClass


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Message (

    val id: String,
    val fact: Fact<*>,
    val handler: Handler? = null

) {

    constructor(fact: Fact<*>, sender: Handler? = null): this(UUID.randomUUID().toString(), fact, sender)

    constructor(message: Message, receiver: Handler): this(UUID.randomUUID().toString(), message.fact, receiver)

    companion object {

        private val mapper = jacksonObjectMapper()

        fun fromJson(json: String): Message {
            return fromJson(mapper.readTree(json))
        }

        internal fun fromJson(tree: JsonNode): Message {
            val id = tree.path("id").textValue()
            val handler = mapper.readValue(mapper.treeAsTokens(tree.get("handler")), Handler::class.java)
            val fact = Fact.fromJson(tree.path("fact"))
            return Message(id, fact, handler)
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

fun Any.toJson(): String {
    return jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)
}

fun <A: Any> List<Message>.applyTo(type: KClass<A>): A {
    return this.map { it.fact }.applyTo(type)
}
