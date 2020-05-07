package io.factdriven.language.definition

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Conditional : Node {

    val condition: (Any.() -> Boolean)?

}

interface Optional: Conditional {

    fun isDefault(): Boolean = condition == null

}

interface Looping: Conditional {

    override val condition: (Any.() -> Boolean)

}
