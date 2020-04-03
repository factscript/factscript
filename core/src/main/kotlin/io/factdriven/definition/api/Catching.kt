package io.factdriven.definition.api

import io.factdriven.execution.Type
import io.factdriven.execution.type
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Catching : Node {

    val catching: KClass<*>

}
