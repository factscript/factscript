package io.factdriven.language

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface Await<T: Any>: AwaitEvent<T>, AwaitFirst<T>

@FlowLang
interface AwaitEvent<T: Any> {

    infix fun <M : Any> event(type: KClass<M>): AwaitEventHaving<T>

}

@FlowLang
interface AwaitFirst<T: Any> {

    infix fun first(path: Flow<T>.() -> Unit): AwaitOr<T>

}

@FlowLang
interface AwaitOr<T: Any> {

    infix fun or(path: Flow<T>.() -> Unit): AwaitOr<T>

}


@FlowLang
interface AwaitEventHaving<T: Any> {

    infix fun having(property: String): AwaitEventHavingMatch<T>

}

@FlowLang
interface AwaitEventHavingMatch<T: Any> {

    infix fun match(value: T.() -> Any?)

}
