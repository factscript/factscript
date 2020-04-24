package io.factdriven.execution

import io.factdriven.language.impl.definition.idSeparator
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
/*
 * Unique, but human manageable and readable name for object and node types
 */
data class Type(val context: String, /* Name unique within given context */ val name: String) {

    companion object {

        private val types = mutableMapOf<Type, KClass<*>>()

        fun from(kClass: KClass<*>): Type {
            val name = kClass.java.name
            val index = name.lastIndexOf('.')
            val type =
                Type(name.substring(0, index), name.substring(index + 1))
            types[type] = kClass
            return type
        }

    }

    override fun toString(): String {
        return "$context$idSeparator$name"
    }

}

val KClass<*>.type: Type
    get() = Type.from(
        this
    )

val Type.kClass: KClass<*> get() {
    return Class.forName("${context}.${name}").kotlin
}

val Type.label: String get() {
    return name.let { local -> local.replace("(.)([A-Z\\d])".toRegex()) { "${it.groupValues[1]} ${it.groupValues[2].toLowerCase()}" } }
}
