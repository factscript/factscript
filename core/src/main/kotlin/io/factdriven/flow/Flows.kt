package io.factdriven.flow

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.factdriven.flow.lang.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
inline fun <reified ENTITY: Entity> define(name: NodeName = ENTITY::class.simpleName!!, type: KClass<ENTITY> = ENTITY::class, definition: Flow<ENTITY>.() -> Unit): DefinedFlow<ENTITY> {

    val flowExecution = FlowImpl<ENTITY>(null).apply(definition)
    flowExecution.name = name
    flowExecution.entityType = type
    Flows.add(flowExecution)
    return flowExecution

}

object Flows {

    private val list = mutableListOf<DefinedFlow<*>>()

    fun add(definition: DefinedFlow<*>) {
        list.add(definition)
    }

    fun all(): List<DefinedFlow<*>> {
        return list
    }

    fun <ENTITY: Entity> get(type: KClass<ENTITY>): DefinedFlow<ENTITY> {
        val definition = list.find {
            it.entityType == type
        } as DefinedFlow<ENTITY>?
        return definition ?: throw IllegalArgumentException()
    }

    fun getElementById(id: NodeId): Node {
        val isSubElement = id.contains("-")
        val definitionId = if (isSubElement) id.substring(0, id.indexOf("-")) else id
        val definition = list.find {
            it.id == definitionId
        } ?: throw java.lang.IllegalArgumentException()
        return if (isSubElement) {
            definition.descendantMap[id] ?: throw java.lang.IllegalArgumentException()
        } else {
            definition
        }
    }

    fun deserialize(message: String): Message<*> {
        val jsonNode = jacksonObjectMapper().readTree(message)
        val factName = jsonNode.get("name").textValue()
        val factType = all().find {
            it.messageType(factName) != null
        }?.messageType(factName)
        if (factType != null) {
            return Message.fromJson(jsonNode, factType)
        }
        throw IllegalArgumentException()
    }

}