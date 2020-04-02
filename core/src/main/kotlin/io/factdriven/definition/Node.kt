package io.factdriven.definition

import io.factdriven.definition.api.*
import io.factdriven.execution.name
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

fun Node.getPromising(): Promising {
    return children.find { it is Promising } as Promising
}

fun Node.getCatching(type: KClass<*>): Consuming {
    val catching = children.find { it is Consuming && it.catching == type } as Consuming?
    return catching ?: throw IllegalArgumentException("Node catching ${type.name} not defined!")
}

fun Node.getThrowing(type: KClass<*>): Throwing {
    val throwing = children.find { it is Throwing && it.throwing == type } as Throwing?
    return throwing ?: throw IllegalArgumentException("Node throwing ${type.name} not defined!")
}

fun Node.getExecuting(type: KClass<*>): Executing {
    val executing = children.find { it is Executing && it.throwing == type } as Executing?
    return executing ?: throw IllegalArgumentException("Node executing ${type.name} not defined!")
}

fun Node.getNodeById(id: String): Node? {
    if (this.id == id)
        return this
    children.forEach {
        val node = it.getNodeById(id)
        if (node != null)
            return node
    }
    return null
}
