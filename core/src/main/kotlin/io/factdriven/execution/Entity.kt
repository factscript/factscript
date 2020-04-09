package io.factdriven.execution

import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun <A: Any> List<Any>.fromJson(type: KClass<A>): A {

    assert (isNotEmpty())

    fun Any.fact(): Fact<*> = if (this is Message) fact else if (this is Fact<*>) this else throw IllegalArgumentException()

    fun apply(fact: Fact<*>, on: KClass<A>): A {
        val constructor = on.constructors.find { it.parameters.size == 1 && it.parameters[0].type.classifier == fact.type.kClass }
        return constructor?.call(fact.details) ?: throw java.lang.IllegalArgumentException()
    }

    fun A.apply(fact: Fact<*>) {
        val method = type.memberFunctions.find { it.parameters.size == 2 && it.parameters[1].type.classifier == fact.type.kClass }
        method?.call(this, fact.details)
    }

    val iterator = iterator()
    val entity = apply(iterator.next().fact(), type)
    iterator.forEachRemaining {
        entity.apply(it.fact())
    }
    return entity

}
