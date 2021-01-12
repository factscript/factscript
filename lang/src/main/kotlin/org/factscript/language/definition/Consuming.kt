package org.factscript.language.definition

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
interface Consuming : Catching {

    val consuming: KClass<*>

}

interface Correlating: Consuming {

    val correlating: Map<String, Any.() -> Any?>

}
