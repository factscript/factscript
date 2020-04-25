package io.factdriven.language.definition

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Throwing: Node {

    val throwing: KClass<*>
    val instance: Any.() -> Any

    fun isSucceeding(): Boolean
    fun isFailing(): Boolean

}
