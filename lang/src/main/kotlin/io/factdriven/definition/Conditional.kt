package io.factdriven.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Conditional : Node {

    val condition: (Any.() -> Boolean)?

}
