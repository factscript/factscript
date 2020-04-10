package io.factdriven.impl.execution

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
/*
 * Unique, but human manageable and readable name for object types
 */
data class Type(val context: String, /* Name unique within given context */ val local: String) {

    companion object {

        private val types = mutableMapOf<Type, KClass<*>>()

        fun from(kClass: KClass<*>): Type {
            val name = kClass.java.name
            val index = name.lastIndexOf('.')
            val type = Type(name.substring(0, index), name.substring(index + 1))
            types[type] = kClass
            return type
        }

        fun from(string: String): Type {
            return from(Class.forName(string).kotlin)
        }

    }

    override fun toString(): String {
        return "$context.$local"
    }

}

val KClass<*>.type: Type get() = Type.from(this)

val Type.kClass: KClass<*> get() {
    return Class.forName(toString()).kotlin
}

val Type.label: String get() {
    return local.let { local -> local.replace("(.)([A-Z\\d])".toRegex()) { "${it.groupValues[1]} ${it.groupValues[2].toLowerCase()}" } }
}
