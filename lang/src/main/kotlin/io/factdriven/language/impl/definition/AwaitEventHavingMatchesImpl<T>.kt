package io.factdriven.language.impl.definition

import io.factdriven.language.AwaitEventHavingMatches
import kotlin.reflect.KProperty1

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class AwaitEventHavingMatchesImpl<T: Any>: AwaitEventHavingMatches<T, Any> {

    val properties: MutableList<String> = mutableListOf()
    val matching: MutableList<T.() -> Any?> = mutableListOf()

    override fun String.match(match: T.() -> Any?) {
        properties.add(this)
        matching.add(match)
    }

    override fun KProperty1<Any, *>.match(match: T.() -> Any?) {
        name.match(match)
    }

}