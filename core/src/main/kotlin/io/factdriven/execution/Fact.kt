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
    val type: Type,
    val details: F

) {

    constructor(fact: F): this(UUID.randomUUID().toString(), fact::class.type, fact) {
        register(fact::class)
    }

    val kClass: KClass<*> @JsonIgnore get() {
        return kClass(type)
    }

    companion object {

        private val types = mutableMapOf<Type, KClass<*>>()

        fun register(type: KClass<*>) {
            types[type.type] = type
        }

        fun kClass(type: Type): KClass<*> {
            return types[type] ?: throw IllegalArgumentException()
        }

        fun fromJson(json: String): Fact<*> {
            return fromJson(jacksonObjectMapper().readTree(json))
        }

        internal fun fromJson(tree: JsonNode): Fact<*> {
            val mapper = jacksonObjectMapper()
            val type = kClass(mapper.readValue(mapper.treeAsTokens(tree.get("type")), Type::class.java))
            mapper.registerSubtypes(type.java)
            val javaType = mapper.typeFactory.constructParametricType(Fact::class.java, type.java)
            return mapper.readValue(mapper.treeAsTokens(tree), javaType)
        }

    }

}

fun <A: Any> List<Fact<*>>.applyTo(type: KClass<A>): A {

    assert (isNotEmpty())

    fun apply(fact: Fact<*>, on: KClass<A>): A {
        val constructor = on.constructors.find { it.parameters.size == 1 && it.parameters[0].type.classifier == fact.kClass }
        return constructor?.call(fact.details) ?: throw java.lang.IllegalArgumentException()
    }

    fun A.apply(fact: Fact<*>) {
        val method = type.memberFunctions.find { it.parameters.size == 2 && it.parameters[1].type.classifier == fact.kClass }
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
