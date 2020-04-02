package io.factdriven.definition.api

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Checking : Executing {

    val condition: Any.() -> Boolean

}
