package io.factdriven.definition

import io.factdriven.definition.Catching

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Awaiting: Catching {

    val properties: List<String>
    val matching: List<Any.() -> Any?>

}
