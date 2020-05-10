package io.factdriven.language.definition

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Throwing: Node, Continuing {

    val throwing: KClass<out Any>
    val factType: FactType
    val factory: Any.() -> Any

}

enum class FactType { Command, Event }
