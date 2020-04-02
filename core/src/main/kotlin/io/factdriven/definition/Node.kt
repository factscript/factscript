package io.factdriven.definition

import io.factdriven.definition.api.*
import io.factdriven.execution.type
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

fun Executing.getPromising(): Promising {
    return children.find { it is Promising } as Promising
}

fun Executing.getCatching(type: KClass<*>): Consuming {
    val catching = children.find { it is Consuming && it.catching == type } as Consuming?
    return catching ?: throw IllegalArgumentException("Node catching ${type.type} not defined!")
}

fun Executing.getThrowing(type: KClass<*>): Throwing {
    val throwing = children.find { it is Throwing && it.throwing == type } as Throwing?
    return throwing ?: throw IllegalArgumentException("Node throwing ${type.type} not defined!")
}

fun Executing.getExecuting(type: KClass<*>): Calling {
    val executing = children.find { it is Calling && it.throwing == type } as Calling?
    return executing ?: throw IllegalArgumentException("Node executing ${type.type} not defined!")
}

fun Executing.getNodeById(id: String): Executing? {
    if (this.id == id)
        return this
    children.forEach {
        val node = it.getNodeById(id)
        if (node != null)
            return node
    }
    return null
}
