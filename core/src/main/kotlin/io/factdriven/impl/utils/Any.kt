package io.factdriven.impl.utils

import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun Any.getValue(property: String): Any? {
    return javaClass.getDeclaredField(property).let { field ->
        field.isAccessible = true
        return@let field.get(this)
    }
}

fun <A: Any> KClass<A>.construct(vararg parameters: Any): A {
    val constructor = constructors.find { constructor ->
        val iterator = parameters.iterator()
        constructor.parameters.all { parameter ->
            parameter.type.classifier == iterator.next()::class
        }
    }
    return constructor?.call(*parameters) ?: throw IllegalArgumentException()
}

fun Any.apply(vararg parameters: Any) {
    val method = this::class.memberFunctions.find { memberFunction ->
        val iterator = parameters.iterator()
        memberFunction.parameters.size == parameters.size + 1
                && memberFunction.parameters.subList(1, memberFunction.parameters.size).all { parameter ->
            parameter.type.classifier == iterator.next()::class
        }
    }
    method?.call(this, *parameters)
}