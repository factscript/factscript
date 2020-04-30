package io.factdriven.language

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface By<T: Any, M: Any> {

    infix fun by(instance: T.() -> M): Any

}
