package io.factdriven.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Checking : Node {

    val condition: Any.() -> Boolean

}
