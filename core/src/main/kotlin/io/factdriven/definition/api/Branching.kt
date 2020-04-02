package io.factdriven.definition.api

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Branching: Executing {

    val gateway: Gateway

}

enum class Gateway { Exclusive }