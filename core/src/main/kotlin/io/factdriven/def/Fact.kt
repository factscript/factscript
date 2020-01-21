package io.factdriven.def

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Fact<F: Any> (

    val id: String,
    val name: String,
    val details: F

) {

    constructor(fact: F): this(UUID.randomUUID().toString(), fact::class.java.simpleName, fact) {
        register(fact::class)
    }

    fun toJson(): String {
        return jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)
    }

    val type: KClass<*> @JsonIgnore get() {
        return getType(name)
    }

    companion object {

        private val types = mutableMapOf<String, KClass<*>>()

        fun register(type: KClass<*>) {
            type.simpleName?.let { types[it] = type } ?: throw IllegalArgumentException()
        }

        fun getType(name: String): KClass<*> {
            return types[name] ?: throw IllegalArgumentException()
        }

        fun fromJson(json: String): Fact<*> {
            return fromJson(jacksonObjectMapper().readTree(json))
        }

        internal fun fromJson(tree: JsonNode): Fact<*> {
            val mapper = jacksonObjectMapper()
            val type = getType(tree.get("name").textValue())
            mapper.registerSubtypes(type.java)
            val javaType = mapper.typeFactory.constructParametricType(Fact::class.java, type.java)
            return mapper.readValue(mapper.treeAsTokens(tree), javaType)
        }

    }

}
