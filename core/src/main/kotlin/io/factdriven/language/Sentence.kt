package io.factdriven.language

import io.factdriven.definition.Throwing

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface Sentence<T: Any> {

    infix fun <M: Any> by(instance: T.() -> M)

}
