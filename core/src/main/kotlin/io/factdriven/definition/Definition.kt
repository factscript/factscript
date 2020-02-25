package io.factdriven.definition

import io.factdriven.execution.name
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Node {

    val entityType: KClass<*>
    val children: List<Child>

    fun getPromising(): Promising {
        return children.find { it is Promising } as Promising
    }

    fun getCatching(type: KClass<*>): Consuming {
        val catching = children.find { it is Consuming && it.catchingType == type } as Consuming?
        return catching ?: throw IllegalArgumentException("Node catching ${type.name} not defined!")
    }

    fun getThrowing(type: KClass<*>): Throwing {
        val throwing = children.find { it is Throwing && it.throwingType == type } as Throwing?
        return throwing ?: throw IllegalArgumentException("Node throwing ${type.name} not defined!")
    }

    fun getExecuting(type: KClass<*>): Executing {
        val executing = children.find { it is Executing && it.throwingType == type } as Executing?
        return executing ?: throw IllegalArgumentException("Node executing ${type.name} not defined!")
    }

    fun getNodeById(id: String): Node? {
        if (this.id == id)
            return this
        children.forEach {
            val node = it.getNodeById(id)
            if (node != null)
                return node
        }
        return null
    }

}

interface Definition: Node {

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
            val definitionId = if (id.contains("-")) id.substring(0, id.indexOf("-")) else id
            val entityType = all.keys.find { it.name == definitionId }
            return if (entityType != null) getDefinitionByType(entityType) else throw IllegalArgumentException("Flow '${definitionId}' is not defined!")
        }

        fun getDefinitionByType(entityType: KClass<*>): Definition {
            return all[entityType] ?: {
                init(entityType)
                all[entityType] ?: throw IllegalArgumentException("Flow '${entityType.name}' is not defined!")
            }.invoke()
        }

        fun getPromisingNodeByCatchingType(catchingType: KClass<*>): Promising {
            return all.values.filter { definition ->
                definition.children.any { it is Promising && it.catchingType == catchingType }
            }[0].getPromising()
        }

        fun getNodeById(id: String): Node {
            return getDefinitionById(id).getNodeById(id) ?: throw IllegalArgumentException("Node '${id}' is not defined!")
        }

        fun init(entityType: KClass<*>) {
            entityType.companionObjectInstance
        }


    }

}

interface Child: Node {

    val parent: Node

}

abstract class NodeImpl(override val entityType: KClass<*>): Node {

    override val children: MutableList<Child> = mutableListOf()

}

open class ChildImpl(override val parent: Node, entityType: KClass<*> = parent.entityType): Child, NodeImpl(entityType)
open class DefinitionImpl(entityType: KClass<*>): Definition, NodeImpl(entityType)
