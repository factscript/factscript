package io.factdriven.flow.lang

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.security.MessageDigest
import java.math.BigInteger
import java.util.*


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias MessageId = String
typealias MessageTarget = Triple<EntityName, EntityId?, MessagePatternHash>

typealias MessagePatterns = List<MessagePattern>
typealias MessagePatternHash = String

fun List<Message<out Fact>>.toJson(): String {
    return jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)
}

interface Messages {

    companion object {

        fun fromJson(messages: String): List<Message<out Fact>> {
            return fromJson(jacksonObjectMapper().readTree(messages))
        }

        fun fromJson(messages: JsonNode): List<Message<out Fact>> {
            return if (messages.isArray) messages.map { Message.fromJson(it) } else listOf(Message.fromJson(messages))
        }

    }

}

data class Message<F: Fact>(

    val id: MessageId,
    val name: FactName,
    val fact: F,
    val target: MessageTarget? = null

) {

    constructor(fact: F): this(UUID.randomUUID().toString(), fact::class.java.simpleName, fact)

    fun toJson(): String {
        return jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)
    }

    fun target(to: MessageTarget): Message<F> {
        return Message(id, name, fact, to)
    }

    companion object {

        inline fun <reified FACT: Fact> fromJson(json: String, factType: FactType<FACT> = FACT::class): Message<FACT> {
            @Suppress("UNCHECKED_CAST")
            return fromJson(jacksonObjectMapper().readTree(json), factType) as Message<FACT>
        }

        fun fromJson(json: JsonNode, factType: FactType<*> = FactTypes.get(json.get("name").textValue())): Message<*> {
            val mapper = jacksonObjectMapper()
            mapper.registerSubtypes(factType.java)
            val type = mapper.typeFactory.constructParametricType(Message::class.java, factType.java)
            return mapper.readValue(mapper.treeAsTokens(json), type)
        }

    }

}

class MessagePattern(
    val entityType: EntityType<*>,
    val name: FactName,
    val properties: Map<Property, Value> = emptyMap()
) {

    constructor(
        entityType: EntityType<*>,
        type: FactType<*>,
        properties: Map<Property, Value> = emptyMap()
    ): this(entityType, type.simpleName!!, properties)

    val hash: String

    init {
        val buffer = StringBuffer(name)
        properties.toSortedMap().forEach {
            buffer.append("|").append(it.key).append("=").append(it.value)
        }
        hash = hash(buffer.toString())
    }

    companion object {

        private val digest = MessageDigest.getInstance("MD5")

        private fun hash(input: String): String {
            val bytes = digest.digest(input.toByteArray())
            return String.format("%0" + (bytes.size shl 1) + "x", BigInteger(1, bytes))
        }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MessagePattern
        if (hash != other.hash) return false
        return true
    }

    override fun hashCode(): Int {
        return hash.hashCode()
    }

}
