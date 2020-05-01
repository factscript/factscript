package io.factdriven.language

import io.factdriven.execution.*
import io.factdriven.language.definition.Flow
import io.factdriven.language.definition.Promising
import io.factdriven.language.impl.definition.PromisingExecutionImpl
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

object Flows {

    private val flows: MutableMap<KClass<*>, Flow> = mutableMapOf()

    fun initialize(type: KClass<*>, vararg types: KClass<*>): List<Flow> {
        val result = mutableListOf(get(type))
        types.forEach { result.add(get(it)) }
        return result
    }

    fun <T: Any> register(flow: PromisingExecution<T>): Flow {
        flows[flow.entity] = flow
        return flow
    }

    inline fun <reified T: Any> register(type: KClass<T> = T::class, flow: PromisingExecution<T>.() -> Unit): Flow {
        val definition = PromisingExecutionImpl(type).apply(flow)
        register(definition)
        return definition
    }

    fun get(id: String): Flow {
        return flows.values.find { it.get(id) != null }?.root ?: throw IllegalArgumentException()
    }

    fun get(type: Type): Flow {
        val entityType = flows.keys.find { it.type == type }
        return if (entityType != null) get(entityType) else throw IllegalArgumentException("Flow '${type}' is not defined!")
    }

    fun get(type: KClass<*>): Flow {
        return find(type) ?: throw IllegalArgumentException("Flow '${type}' is not defined!")
    }

    fun find(type: KClass<*>? = null, handling: KClass<*>? = null, reporting: KClass<*>? = null): Flow? {
        type?.companionObjectInstance
        val flows = flows
        val result =  flows.values.filter { definition ->
            (definition.entity == type || type == null) && ((handling == null && reporting == null) || (definition.children.any { it is Promising && (it.consuming == handling || handling == null) && (it.succeeding == reporting || it.failing.contains(reporting) || reporting == null) }))
        }
        return result.firstOrNull()
    }

    fun all(): Iterable<Flow> {
        return flows.values.asIterable()
    }

    fun handling(message: Message): List<Receptor> {
        return all().map { it.findReceptorsFor(message) }.flatten()
    }

}

inline fun <reified T: Any> flow(type: KClass<T> = T::class, flow: PromisingExecution<T>.() -> Unit): Flow {
    return Flows.register(type, flow)
}