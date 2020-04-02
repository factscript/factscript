package io.factdriven.definition.api

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Conditional : Node {

    val condition: Any.() -> Boolean

}

enum class Gateway { Exclusive }