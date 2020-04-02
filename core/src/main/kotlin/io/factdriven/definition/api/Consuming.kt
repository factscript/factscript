package io.factdriven.definition.api

import io.factdriven.definition.api.Catching

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Consuming: Catching {

    val properties: List<String>
    val matching: List<Any.() -> Any?>

}
