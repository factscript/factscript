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

fun <ENTITY: Entity> EntityType<ENTITY>.apply(any: List<Any>): ENTITY {

    assert (any.isNotEmpty())

    fun <ENTITY: Entity> apply(any: Any, on: KClass<out ENTITY>): ENTITY {
        val fact = if (any is Message<*>) any.fact else any
        val constructor = on.constructors.find { it.parameters.size == 1 && it.parameters[0].type.classifier == fact::class }
        return constructor?.call(fact) ?: throw IllegalArgumentException()
    }

    fun Entity.apply(any: Any) {
        val fact = if (any is Message<*>) any.fact else any
        val method = this::class.memberFunctions.find { it.parameters.size == 2 && it.parameters[1].type.classifier == fact::class }
        method?.call(this, fact)
    }

    val iterator = any.iterator()
    val entity = apply(iterator.next(), this)
    iterator.forEachRemaining {
        entity.apply(it)
    }
    return entity

}