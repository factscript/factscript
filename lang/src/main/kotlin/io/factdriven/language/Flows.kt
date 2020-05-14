package io.factdriven.language

import io.factdriven.execution.*
import io.factdriven.language.definition.Flow
import io.factdriven.language.definition.Promising
import io.factdriven.language.impl.definition.*
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

object Flows {

    private val active: MutableMap<KClass<*>, Flow> = mutableMapOf()
    private val cached: MutableMap<KClass<*>, Flow> = mutableMapOf()

    fun activate(vararg types: KClass<*>): List<Flow> {
        types.forEach {
            it.companionObjectInstance
            active[it] = cached[it]!!
        }
        active.keys.filter { !(types.contains(it)) }.forEach { active.remove(it) }
        return active.values.toList()
    }

    fun <T: Any> register(flow: Promise<T>): Flow {
        cached[flow.entity] = flow
        active[flow.entity] = flow
        return flow
    }

    inline fun <reified T: Any> register(type: KClass<T> = T::class, flow: Promise<T>.() -> Unit): Flow {
        val definition = PromisingFlowImpl(type).apply(flow)
        register(definition)
        return definition
    }

    fun get(id: String): Flow {
        return active.values.find { it.get(id) != null }?.root ?: throw IllegalArgumentException()
    }

    fun get(type: Type): Flow {
        val entityType = active.keys.find { it.type == type }
        return if (entityType != null) get(entityType) else throw IllegalArgumentException("Flow '${type}' is not defined!")
    }

    fun get(type: KClass<*>): Flow {
        return find(type) ?: throw IllegalArgumentException("Flow '${type}' is not defined!")
    }

    fun find(type: KClass<*>? = null, handling: KClass<*>? = null, reporting: KClass<*>? = null): Flow? {
        val flows = active
        val result =  flows.values.filter { definition ->
            (definition.entity == type || type == null) && (
                (handling == null && reporting == null) ||
                (definition.children.any {
                    it is PromisingImpl<*>
                        && (it.consuming == handling || handling == null)
                        && (it.successType == reporting || it.failureTypes.contains(reporting) || reporting == null)
                }))
        }
        return result.firstOrNull()
    }

    fun all(): Iterable<Flow> {
        return active.values.asIterable()
    }

    fun findReceptorsFor(message: Message): Set<Receptor> {
        return all().map { it.findReceptorsFor(message) }.flatten().toSet()
    }

}

inline fun <reified T: Any> flow(type: KClass<T> = T::class, flow: Promise<T>.() -> Unit): Flow {
    return Flows.register(type, flow)
}