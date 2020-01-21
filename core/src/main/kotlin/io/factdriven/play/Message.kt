package io.factdriven.play

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.factdriven.def.Fact
import java.util.*


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Message (

    val id: String,
    val fact: Fact<*>,
    val sender: Endpoint? = null,
    val receiver: Endpoint? = null

) {

    constructor(fact: Fact<*>, source: Endpoint? = null): this(UUID.randomUUID().toString(), fact, source)

    constructor(message: Message, target: Endpoint): this(message.id, message.fact, message.sender, target)

    fun toJson(): String {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
    }

    companion object {

        private val mapper = jacksonObjectMapper()

        fun fromJson(json: String): Message {
            return fromJson(mapper.readTree(json))
        }

        internal fun fromJson(tree: JsonNode): Message {
            val id = tree.path("id").textValue()
            val sender = mapper.readValue(mapper.treeAsTokens(tree.get("sender")), Endpoint::class.java)
            val receiver = mapper.readValue(mapper.treeAsTokens(tree.get("receiver")), Endpoint::class.java)
            val fact = Fact.fromJson(tree.path("fact"))
            return Message(id, fact, sender, receiver)
        }

    }

    object list {

        fun fromJson(json: String): List<Message> {
            val tree = mapper.readTree(json)
            return if (tree.isArray) tree.map { fromJson(it) } else listOf(Message.fromJson(json))
        }

    }

}

fun List<Message>.toJson(): String {
    return jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)
}
