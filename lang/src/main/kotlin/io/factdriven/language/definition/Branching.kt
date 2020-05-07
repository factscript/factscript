package io.factdriven.language.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Branching: Node {

    val split: Split

    fun isConditional(): Boolean

}

enum class Split { Exclusive, Inclusive, Parallel, Waiting }