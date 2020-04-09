package io.factdriven.implementation.utils

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun Any.getValue(property: String): Any? {
    return javaClass.getDeclaredField(property).let { field ->
        field.isAccessible = true
        return@let field.get(this)
    }
}