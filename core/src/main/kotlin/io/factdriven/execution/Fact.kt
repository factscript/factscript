package io.factdriven.execution

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Fact<F: Any> (

    val id: String,
    val name: String,
    val details: F

) {

    constructor(fact: F): this(UUID.randomUUID().toString(), fact.name, fact) {
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
            types[type.name] = type
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

fun <A: Any> List<Fact<*>>.applyTo(type: KClass<A>): A {

    assert (isNotEmpty())

    fun apply(fact: Fact<*>, on: KClass<A>): A {
        val constructor = on.constructors.find { it.parameters.size == 1 && it.parameters[0].type.classifier == fact.type }
        return constructor?.call(fact.details) ?: throw java.lang.IllegalArgumentException()
    }

    fun A.apply(fact: Fact<*>) {
        val method = type.memberFunctions.find { it.parameters.size == 2 && it.parameters[1].type.classifier == fact.type }
        method?.call(this, fact.details)
    }

    val iterator = iterator()
    val entity = apply(iterator.next(), type)
    iterator.forEachRemaining {
        entity.apply(it)
    }
    return entity

}

fun Any.getValue(property: String): Any? {
    return javaClass.getDeclaredField(property).let { field ->
        field.isAccessible = true
        return@let field.get(this)
    }
}
