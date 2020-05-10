package io.factdriven.language.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Branching: Node, Continuing {

    val fork: Junction
    val join: Junction?

}

enum class Junction {

    One, Some, All, First;

    fun isConditional(): Boolean = this == One || this == Some
    fun isWaiting(): Boolean = this == First
    fun isNotWaiting(): Boolean = !isWaiting()
    fun isExclusive(): Boolean = this == One || this == First
    fun isConcurrent(): Boolean = !isExclusive()

}