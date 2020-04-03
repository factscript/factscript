package io.factdriven.definition.api

import io.factdriven.execution.type

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Branching: Node {

    val gateway: Gateway

}

enum class Gateway { Exclusive }