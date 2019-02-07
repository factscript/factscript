package io.factdriven.flow.lang

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.security.MessageDigest
import java.math.BigInteger
import java.util.*


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias Messages = List<Message<*>>
typealias MessageId = String
typealias MessageTarget = Triple<EntityName, EntityId?, MessagePatternHash>

typealias MessagePatterns = Set<MessagePattern>
typealias MessagePatternHash = String

data class Message<F: Fact>(

    val id: MessageId,
    val name: FactName,
    val fact: F,
    val target: MessageTarget? = null

) {

    constructor(fact: F): this(UUID.randomUUID().toString(), fact::class.java.simpleName, fact)

    fun toJson(): String {
        return jacksonObjectMapper().writeValueAsString(this)
    }

    fun target(to: MessageTarget): Message<F> {
        return Message(id, name, fact, to)
    }

    companion object {

        fun <F: Fact> fromJson(json: String, factType: FactType<F>): Message<F> {
            val mapper = jacksonObjectMapper()
            mapper.registerSubtypes(factType.java)
            val type = mapper.typeFactory.constructParametricType(Message::class.java, factType.java)
            return mapper.readValue(json, type)
        }

        fun <F: Fact> fromJson(json: JsonNode, factType: FactType<F>): Message<F> {
            val mapper = jacksonObjectMapper()
            mapper.registerSubtypes(factType.java)
            val type = mapper.typeFactory.constructParametricType(Message::class.java, factType.java)
            return mapper.readValue(mapper.treeAsTokens(json), type)
        }

    }

}

data class MessagePattern(
    val name: FactName,
    val properties: Map<Property, Value> = emptyMap()
) {

    constructor(
        type: FactType<*>,
        properties: Map<Property, Value> = emptyMap()
    ): this(type.simpleName!!, properties) // TODO simple name is just fallback

    val hash: String

    init {
        val buffer = StringBuffer(name)
        properties.toSortedMap().forEach {
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
