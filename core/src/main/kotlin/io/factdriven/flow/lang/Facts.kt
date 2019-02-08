package io.factdriven.flow.lang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias Fact = Any
typealias Facts = List<Fact>
typealias FactType<FACT> = KClass<out FACT>
typealias FactName = String

typealias Property = String
typealias Value = Any?

fun Fact.getValue(property: Property): Value {
    return javaClass.getDeclaredField(property).let {
        it.isAccessible = true
        it.get(this)
    }
}

object FactTypes {

    private val types = mutableMapOf<FactName, FactType<*>>()

    fun add(type: FactType<*>) {
        type.simpleName?.let { types[it] = type } ?: throw IllegalArgumentException()
    }

    fun get(type: FactName): FactType<*> {
        return types[type] ?: throw IllegalArgumentException()
    }

}
