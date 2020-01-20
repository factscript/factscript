package io.factdriven.def

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

}

interface Definition: Node

interface Child: Node {

    val parent: Node

}

abstract class NodeImpl(override val entityType: KClass<*>): Node {

    override val children: MutableList<Child> = mutableListOf()

}

open class ChildImpl(override val parent: Node, override val entityType: KClass<*> = parent.entityType): Child, NodeImpl(entityType)
open class DefinitionImpl(override val entityType: KClass<*>): Definition, NodeImpl(entityType)
