package io.factdriven.definition

import io.factdriven.definition.Node
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Throwing: Node {

    val throwing: KClass<*>
    val instance: Any.() -> Any

}
