package io.factdriven.def

import io.factdriven.lang.Flow
import io.factdriven.play.Handling
import io.factdriven.play.Message
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Node {

    val entityType: KClass<*>
    val children: List<Child>

    fun getCatching(type: KClass<*>): Catching {
        val catching = children.find { it is Catching && it.catchingType == type } as Catching?
        return catching ?: throw IllegalArgumentException("Node catching ${type.simpleName} not defined!")
    }

    fun getThrowing(type: KClass<*>): Throwing {
        val throwing = children.find { it is Throwing && it.throwingType == type } as Throwing?
        return throwing ?: throw IllegalArgumentException("Node throwing ${type.simpleName} not defined!")
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

        fun register(definition: Definition) {
            (all as MutableMap<KClass<*>, Definition>)[definition.entityType] = definition
        }

        fun getDefinitionById(id: String): Definition {
            val definitionId = if (id.contains("-")) id.substring(0, id.indexOf("-")) else id
            val entityType = all.keys.find { it.simpleName == definitionId }
            return if (entityType != null) getDefinitionByType(entityType) else throw IllegalArgumentException("Flow '${definitionId}' is not defined!")
        }

        fun getDefinitionByType(entityType: KClass<*>): Definition {
            return all[entityType] ?: {
                Flow.init(entityType)
                all[entityType] ?: throw IllegalArgumentException("Flow '${entityType.simpleName}' is not defined!")
            }.invoke()
        }

        fun getNodeById(id: String): Node {
            return getDefinitionById(id).getNodeById(id) ?: throw IllegalArgumentException("Node '${id}' is not defined!")
        }

    }

}

interface Child: Node {

    val parent: Node

}

abstract class NodeImpl(override val entityType: KClass<*>): Node {

    override val children: MutableList<Child> = mutableListOf()

}

open class ChildImpl(override val parent: Node, override val entityType: KClass<*> = parent.entityType): Child, NodeImpl(entityType)
open class DefinitionImpl(override val entityType: KClass<*>): Definition, NodeImpl(entityType)
