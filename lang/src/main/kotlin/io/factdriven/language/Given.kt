package io.factdriven.language

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface Given<T: Any>: GivenCondition<T>, Labeled<Given<T>>

@FlowLang
interface GivenCondition<T: Any> {

    infix fun condition(condition: T.() -> Boolean): GivenCondition<T>

}
