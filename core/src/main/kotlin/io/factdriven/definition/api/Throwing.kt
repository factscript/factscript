package io.factdriven.definition.api

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Throwing: Executing {

    val throwing: KClass<*>
    val instance: Any.() -> Any

}
