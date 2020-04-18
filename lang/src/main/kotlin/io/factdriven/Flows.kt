package io.factdriven

import io.factdriven.definition.Promising
import io.factdriven.execution.Message
import io.factdriven.execution.Receptor
import io.factdriven.execution.Type
import io.factdriven.execution.type
import io.factdriven.impl.definition.FlowImpl
import io.factdriven.impl.definition.idSeparator
import io.factdriven.language.Flow
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

object Flows {

    private val flows: MutableMap<KClass<*>, io.factdriven.definition.Flow> = mutableMapOf()

    fun initialize(type: KClass<*>, vararg types: KClass<*>): List<io.factdriven.definition.Flow> {
        val result = mutableListOf(get(type))
        types.forEach { result.add(get(it)) }
        return result
    }

    fun <T: Any> register(flow: Flow<T>): Flow<T> {
        flows[flow.entity] = flow
        return flow
    }

    inline fun <reified T: Any> register(type: KClass<T> = T::class, flow: Flow<T>.() -> Unit): Flow<T> {
        val definition = FlowImpl(type).apply(flow)
        register(definition)
        return definition
    }

    fun get(id: String): io.factdriven.definition.Flow {
        return flows.values.find { it.get(id) != null }?.root ?: throw IllegalArgumentException()
    }

    fun get(type: Type): io.factdriven.definition.Flow {
        val entityType = flows.keys.find { it.type == type }
        return if (entityType != null) get(entityType) else throw IllegalArgumentException("Flow '${type}' is not defined!")
    }

    fun get(type: KClass<*>? = null, handling: KClass<*>? = null): io.factdriven.definition.Flow {
        type?.companionObjectInstance
        val result =  flows.values.filter { definition ->
            (definition.entity == type || type == null) && definition.children.any { it is Promising && (it.catching == handling || handling == null) }
        }
        return if (result.isNotEmpty()) result[0] else throw IllegalArgumentException("Flow '${type}' is not defined!")
    }

    fun all(): Iterable<io.factdriven.definition.Flow> {
        return flows.values.asIterable()
    }

    fun handling(message: Message): List<Receptor> {
        return Flows.all().map { it.findReceptorsFor(message) }.flatten()
    }

}

inline fun <reified T: Any> flow(type: KClass<T> = T::class, flow: Flow<T>.() -> Unit): Flow<T> {
    return Flows.register(type, flow)
}