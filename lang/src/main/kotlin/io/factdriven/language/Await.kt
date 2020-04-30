package io.factdriven.language

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Await<T: Any>: AwaitEvent<T>, AwaitFirst<T>

@FlowLanguage
interface AwaitEvent<T: Any> {

    infix fun <M : Any> event(type: KClass<M>): AwaitEventHaving<T>

}

@FlowLanguage
interface AwaitFirst<T: Any> {

    infix fun first(path: AwaitingExecution<T>.() -> Unit): AwaitOr<T>

}

@FlowLanguage
interface AwaitOr<T: Any> {

    infix fun or(path: AwaitingExecution<T>.() -> Unit): AwaitOr<T>

}


@FlowLanguage
interface AwaitEventHaving<T: Any> {

    infix fun having(property: String): AwaitEventHavingMatch<T>

}

@FlowLanguage
interface AwaitEventHavingMatch<T: Any> {

    infix fun match(value: T.() -> Any?)

}
