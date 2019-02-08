package io.factdriven.flow

import io.factdriven.flow.lang.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
inline fun <reified ENTITY: Entity> define(name: NodeName = ENTITY::class.simpleName!!, type: KClass<ENTITY> = ENTITY::class, definition: UnclassifiedFlow<ENTITY>.() -> Unit): Flow<ENTITY> {

    val flowExecution = FlowImpl<ENTITY>(null).apply(definition)
    flowExecution.name = name
    flowExecution.type = type
    (Flows.all as MutableList).add(flowExecution)
    return flowExecution

}

object Flows {

    val all: List<Flow<*>> = mutableListOf()

    inline fun <reified ENTITY: Entity> get(type: EntityType<ENTITY> = ENTITY::class): Flow<ENTITY> {
        @Suppress("UNCHECKED_CAST")
        return all.find { it.type == type } as Flow<ENTITY>? ?: throw IllegalArgumentException()
    }

    fun get(id: NodeId): Flow<*> {
        val flowId = if (id.contains("-")) id.substring(0, id.indexOf("-")) else id
        return all.find { it.id == flowId } ?: throw IllegalArgumentException()
    }

    fun <FACT: Fact> match(message: Message<FACT>): MessagePatterns {
        return all.map { it.match(message) }.flatten()
    }

}