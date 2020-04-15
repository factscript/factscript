package io.factdriven.language

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface Consume<T: Any>: ConsumeEvent<T>, ConsumeFirst<T>

@FlowLang
interface ConsumeEvent<T: Any> {

    infix fun <M : Any> event(type: KClass<M>): ConsumeEventHaving<T>

}

@FlowLang
interface ConsumeFirst<T: Any> {

    infix fun first(path: Flow<T>.() -> Unit): ConsumeOr<T>

}

@FlowLang
interface ConsumeOr<T: Any> {

    infix fun or(path: Flow<T>.() -> Unit): ConsumeOr<T>

}


@FlowLang
interface ConsumeEventHaving<T: Any> {

    infix fun having(property: String): ConsumeEventHavingMatch<T>

}

@FlowLang
interface ConsumeEventHavingMatch<T: Any> {

    infix fun match(value: T.() -> Any?)

}
