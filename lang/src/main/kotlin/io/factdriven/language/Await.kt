package io.factdriven.language

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Await<T: Any>: AwaitEvent<T>, AwaitFirst<T>, AwaitTime<T>

@FlowLanguage
interface AwaitEvent<T: Any> {

    infix fun <M : Any> event(type: KClass<M>): AwaitEventHaving<T, M>

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
interface AwaitEventHaving<T: Any, M: Any>: AwaitEventBut<T> {

    infix fun having(property: String): AwaitEventHavingMatch<T>
    infix fun having(property: KProperty1<M, *>): AwaitEventHavingMatch<T>
    infix fun having(map: AwaitEventHavingMatches<T, M>.() -> Unit): AwaitEventBut<T>

}

@FlowLanguage
interface AwaitEventHavingMatches<T: Any, M: Any> {

    infix fun String.match (match: T.() -> Any?)
    infix fun KProperty1<M, *>.match(match: T.() -> Any?)

}

@FlowLanguage
interface AwaitEventHavingMatch<T: Any> {

    infix fun match(value: T.() -> Any?): AwaitEventBut<T>

}

@FlowLanguage
interface AwaitEventBut<T: Any> {

    infix fun but(path: AwaitingExecution<T>.() -> Unit): AwaitEventBut<T>

}
