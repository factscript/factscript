package io.factdriven.definition

import io.factdriven.definition.Catching

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Consuming: Catching {

    val properties: List<String>
    val matching: List<Any.() -> Any?>

}