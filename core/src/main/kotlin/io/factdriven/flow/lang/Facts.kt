package io.factdriven.flow.lang

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias Fact = Any
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
