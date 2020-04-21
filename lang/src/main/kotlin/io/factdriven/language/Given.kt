package io.factdriven.language

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Given<T: Any>: GivenCondition<T>, Labeled<Given<T>>

@FlowLanguage
interface GivenCondition<T: Any> {

    infix fun condition(condition: T.() -> Boolean)

}
