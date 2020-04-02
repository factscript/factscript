package io.factdriven.definition

import io.factdriven.definition.api.Node
import io.factdriven.definition.api.Promising
import io.factdriven.execution.Name
import io.factdriven.execution.name
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Definition: Node

interface Definitions {

    companion object {

        val all: Map<KClass<*>, Definition> = mutableMapOf()

        fun register(vararg definitions: Definition) {
            definitions.forEach { definition ->
                all.keys.filter { it.name == definition.typeName }.forEach {
                    (all as MutableMap<KClass<*>, Definition>).remove(it)
                }
                (all as MutableMap<KClass<*>, Definition>)[definition.entityType] = definition
            }
        }

        fun getDefinitionById(id: String): Definition {
            return getDefinitionByName(Name(id.split("-")[0], id.split("-")[1]))
        }

        fun getDefinitionByName(name: Name): Definition {
            val entityType = all.keys.find { it.name == name }
            return if (entityType != null) getDefinitionByType(entityType) else throw IllegalArgumentException("Flow '${name}' is not defined!")
        }

        fun getDefinitionByType(entityType: KClass<*>): Definition {
            return all[entityType] ?: {
                init(entityType)
                all[entityType] ?: throw IllegalArgumentException("Flow '${entityType.name}' is not defined!")
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
