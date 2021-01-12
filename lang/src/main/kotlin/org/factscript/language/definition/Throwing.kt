package org.factscript.language.definition

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Throwing: Node, Continuing {

    val throwing: KClass<out Any>
    val factType: FactType
    val factQuality: FactQuality?
    val factory: Any.() -> Any

}

enum class FactType { Command, Event }
enum class FactQuality { Success, Failure }
