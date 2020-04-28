package io.factdriven.language.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Branching: Node {

    val gateway: Gateway

    fun isConditional(): Boolean

}

enum class Gateway { Exclusive, Inclusive, Parallel, Catching }