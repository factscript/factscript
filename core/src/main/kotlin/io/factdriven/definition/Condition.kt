package io.factdriven.definition

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Condition : Child {

    val condition: Any.() -> Boolean

}

open class ConditionImpl(parent: Node): Condition, ChildImpl(parent) {

    override lateinit var condition: Any.() -> Boolean

}