package io.factdriven.language.definition

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Consuming : Catching {

    val consuming: KClass<*>

}

interface Correlating: Consuming {

    val properties: List<String>
    val matching: List<Any.() -> Any?>

}
