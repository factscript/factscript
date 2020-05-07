package io.factdriven.language.definition

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Throwing: Node, Continuing {

    val throwing: KClass<out Any>

    val instance: Any.() -> Any
    val throwingType: FactType

}

enum class FactType { Command, Event }
