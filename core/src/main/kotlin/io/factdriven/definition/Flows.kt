package io.factdriven.definition

import io.factdriven.definition.api.Flowing
import io.factdriven.definition.api.Node
import io.factdriven.definition.api.Promising
import io.factdriven.execution.Type
import io.factdriven.execution.type
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

interface Flows {

    companion object {

        val all: Map<KClass<*>, Flowing> = mutableMapOf()

        fun register(vararg flowings: Flowing) {
            flowings.forEach { definition ->
                all.keys.filter { it.type == definition.type }.forEach {
                    (all as MutableMap<KClass<*>, Flowing>).remove(it)
                }
                (all as MutableMap<KClass<*>, Flowing>)[definition.entityType] = definition
            }
        }

        fun getDefinitionById(id: String): Flowing {
            return getDefinitionByName(Type(id.split("-")[0], id.split("-")[1]))
        }

        fun getDefinitionByName(type: Type): Flowing {
            val entityType = all.keys.find { it.type == type }
            return if (entityType != null) getDefinitionByType(entityType) else throw IllegalArgumentException("Flow '${type}' is not defined!")
        }

        fun getDefinitionByType(entityType: KClass<*>): Flowing {
            return all[entityType] ?: {
                init(entityType)
                all[entityType] ?: throw IllegalArgumentException("Flow '${entityType.type}' is not defined!")
            }.invoke()
        }

        fun getPromisingNodeByCatchingType(catchingType: KClass<*>): Promising {
            return all.values.filter { definition ->
                definition.children.any { it is Promising && it.catching == catchingType }
            }[0].getPromising()
        }

        fun getNodeById(id: String): Node {
            return getDefinitionById(id).getNodeById(id) ?: throw IllegalArgumentException("Node '${id}' is not defined!")
        }

        fun clear() {
            (all as MutableMap).clear()
        }

        fun init(entityType: KClass<*>) {
            entityType.companionObjectInstance
        }


    }

}
