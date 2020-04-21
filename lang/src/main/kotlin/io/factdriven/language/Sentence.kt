package io.factdriven.language

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Sentence<T: Any> {

    infix fun <M: Any> by(instance: T.() -> M)

}
