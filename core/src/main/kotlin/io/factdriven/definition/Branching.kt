package io.factdriven.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Branching: Node {

    val gateway: Gateway

}

enum class Gateway { Exclusive }