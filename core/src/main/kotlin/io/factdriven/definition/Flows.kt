package io.factdriven.definition

import io.factdriven.definition.api.*
import io.factdriven.execution.*
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

interface Flows {

    companion object {

        private val all: MutableMap<KClass<*>, Flowing> = mutableMapOf()

        fun register(vararg flowing: Flowing) {
            flowing.forEach {
                all[it.entity] = it
            }
        }

        fun init(vararg type: KClass<*>) {
            type.forEach {
                it.companionObjectInstance
            }
        }

        fun get(id: String): Flowing {
            return findByType(Type(id.split("-")[0], id.split("-")[1]))
        }

        fun all(): Iterable<Flowing> {
            return all.values.asIterable()
        }

        fun <N: Node> find(id: String, type: KClass<in N> = Node::class): N {
            return get(id).findById(id) ?: throw IllegalArgumentException("Node '${id}' is not defined!")
        }

        fun findByType(type: Type): Flowing {
            val entityType = all.keys.find { it.type == type }
            return if (entityType != null) findByClass(entityType) else throw IllegalArgumentException("Flow '${type}' is not defined!")
        }

        fun findByClass(type: KClass<*>): Flowing {
            return all[type] ?: {
                init(type)
                all[type] ?: throw IllegalArgumentException("Flow '${type.type}' is not defined!")
            }.invoke()
        }

        fun findCatching(catchingType: KClass<*>): Flowing {
            return all.values.filter { definition ->
                definition.children.any { it is Promising && it.catching == catchingType }
            }[0]
        }

        fun handling(message: Message): List<Handling> {
            return all.values.map {
                it.handling(message)
            }.flatten()
        }

    }

}
