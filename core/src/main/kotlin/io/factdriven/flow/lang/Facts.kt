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

fun Fact.getProperty(propertyName: Property): Value {
    return javaClass.getDeclaredField(propertyName).let {
        it.isAccessible = true
        it.get(this)
    }
}
