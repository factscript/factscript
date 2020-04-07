package io.factdriven

import io.factdriven.definition.api.*
import io.factdriven.execution.*
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

object Flows {

    private val all: MutableMap<KClass<*>, Flowing> = mutableMapOf()

    fun register(vararg flowing: Flowing) {
        flowing.forEach {
            all[it.entity] = it
        }
    }

    fun init(type: KClass<*>, vararg types: KClass<*>): List<Flowing> {
        val result = mutableListOf(get(type))
        types.forEach { result.add(get(it)) }
        return result
    }

    fun all(): Iterable<Flowing> {
        return all.values.asIterable()
    }

    fun get(id: String): Flowing {
        return get(Type(id.split("-")[0], id.split("-")[1]))
    }

    fun get(type: Type): Flowing {
        val entityType = all.keys.find { it.type == type }
        return if (entityType != null) get(entityType) else throw IllegalArgumentException("Flow '${type}' is not defined!")
    }

    fun get(type: KClass<*>? = null, handling: KClass<*>? = null): Flowing {
        type?.companionObjectInstance
        val result =  all.values.filter { definition ->
            (definition.entity == type || type == null) && definition.children.any { it is Promising && (it.catching == handling || handling == null) }
        }
        return if (result.isNotEmpty()) result[0] else throw IllegalArgumentException("Flow '${type}' is not defined!")
    }

    fun handling(message: Message): List<Handling> {
        return all.values.map { it.handling(message) }.flatten()
    }

}
