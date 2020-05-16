package io.factdriven.language

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Repeat<T: Any> {

    operator fun invoke(path: Loop<T>.() -> Unit)

}
