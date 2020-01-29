package io.factdriven.def

import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Node {

    val entityType: KClass<*>
    val children: List<Child>

    fun getPromisingOn(type: KClass<*>): Promising {
        val promising = children.find { it is Promising && it.catchingType == type } as Promising?
        return promising ?: throw IllegalArgumentException("Node promising on ${type.simpleName} not defined!")
    }

    fun getCatching(type: KClass<*>): Catching {
        val catching = children.find { it is Catching && it.catchingType == type } as Catching?
        return catching ?: throw IllegalArgumentException("Node catching ${type.simpleName} not defined!")
    }

    fun getThrowing(type: KClass<*>): Throwing {
        val throwing = children.find { it is Throwing && it.throwingType == type } as Throwing?
        return throwing ?: throw IllegalArgumentException("Node throwing ${type.simpleName} not defined!")
    }

    fun getExecuting(type: KClass<*>): Executing {
        val executing = children.find { it is Executing && it.throwingType == type } as Executing?
        return executing ?: throw IllegalArgumentException("Node executing ${type.simpleName} not defined!")
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
                all.keys.filter { it.simpleName == definition.typeName }.forEach {
                    (all as MutableMap<KClass<*>, Definition>).remove(it)
                }
                (all as MutableMap<KClass<*>, Definition>)[definition.entityType] = definition
            }
        }

        fun getDefinitionById(id: String): Definition {
            val definitionId = if (id.contains("-")) id.substring(0, id.indexOf("-")) else id
            val entityType = all.keys.find { it.simpleName == definitionId }
            return if (entityType != null) getDefinitionByType(entityType) else throw IllegalArgumentException("Flow '${definitionId}' is not defined!")
        }

        fun getDefinitionByType(entityType: KClass<*>): Definition {
            return all[entityType] ?: {
                init(entityType)
                all[entityType] ?: throw IllegalArgumentException("Flow '${entityType.simpleName}' is not defined!")
            }.invoke()
        }

        fun getPromisingNodeByCatchingType(catchingType: KClass<*>): Promising {
            return all.values.filter { definition ->
                definition.children.any { it is Promising && it.catchingType == catchingType }
            }[0].getPromisingOn(catchingType)
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
