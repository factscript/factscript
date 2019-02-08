package io.factdriven.flow

import io.factdriven.flow.lang.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
inline fun <reified ENTITY: Entity> define(name: NodeName = ENTITY::class.simpleName!!, type: KClass<ENTITY> = ENTITY::class, definition: Flow<ENTITY>.() -> Unit): DefinedFlow<ENTITY> {

    val flowExecution = FlowImpl<ENTITY>(null).apply(definition)
    flowExecution.name = name
    flowExecution.entityType = type
    (Flows.all as MutableList).add(flowExecution)
    return flowExecution

}

object Flows {

    val all: List<DefinedFlow<*>> = mutableListOf()

    inline fun <reified ENTITY: Entity> get(type: EntityType<ENTITY> = ENTITY::class): DefinedFlow<ENTITY> {
        @Suppress("UNCHECKED_CAST")
        return all.find { it.entityType == type } as DefinedFlow<ENTITY>? ?: throw IllegalArgumentException()
    }

    fun get(id: NodeId): DefinedFlow<*> {
        val isSubElement = id.contains("-")
        val flowId = if (isSubElement) id.substring(0, id.indexOf("-")) else id
        return all.find { it.id == flowId } ?: throw IllegalArgumentException()
    }

}