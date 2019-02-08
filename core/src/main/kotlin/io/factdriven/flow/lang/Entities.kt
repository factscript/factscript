package io.factdriven.flow.lang

import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
typealias Entity = Any
typealias EntityId = String
typealias EntityName = String
typealias EntityType<ENTITY> = KClass<out ENTITY>

fun <ENTITY: Entity> apply(facts: Facts, on: EntityType<ENTITY>): ENTITY {

    assert (facts.isNotEmpty())

    fun <ENTITY: Entity> apply(any: Any, on: KClass<out ENTITY>): ENTITY {
        val fact = if (any is Message<*>) any.fact else any
        val constructor = on.constructors.find { it.parameters.size == 1 && it.parameters[0].type.classifier == fact::class }
        return constructor?.call(fact) ?: throw IllegalArgumentException()
    }

    fun Entity.apply(fact: Fact) {
        val fact = if (fact is Message<*>) fact.fact else fact
        val method = this::class.memberFunctions.find { it.parameters.size == 2 && it.parameters[1].type.classifier == fact::class }
        method?.call(this, fact)
    }

    val iterator = facts.iterator()
    val entity = apply(iterator.next(), on)
    iterator.forEachRemaining {
        entity.apply(it)
    }
    return entity

}