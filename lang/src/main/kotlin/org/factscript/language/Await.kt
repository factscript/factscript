package org.factscript.language

import org.factscript.language.impl.definition.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Await<T: Any>: AwaitEvent<T>, AwaitFirst<T>, AwaitTime<T>

@FlowLanguage
interface AwaitEvent<T: Any>: AwaitEventSuccess<T>, AwaitEventFailure<T>

infix fun <T: Any, M: Any> AwaitEvent<T>.event(type: KClass<M>): AwaitEventHaving<T, M> {
    return (this as CorrelatingImpl<T>).event(type)
}

@FlowLanguage
interface AwaitEventSuccess<T: Any> {

    infix fun <M: Any> success(event: KClass<M>)

}

@FlowLanguage
interface AwaitEventFailure<T: Any> {

    infix fun <M: Any> failure(event: KClass<M>)

}

@FlowLanguage
interface AwaitFirst<T: Any> {

    infix fun first(path: Catch<T>.() -> Unit): AwaitOr<T>

}

@FlowLanguage
interface AwaitOr<T: Any> {

    infix fun or(path: Catch<T>.() -> Unit): AwaitOr<T>

}

@FlowLanguage
interface AwaitEventHaving<T: Any, M: Any>: AwaitEventBut<T> {

    infix fun having(property: String): AwaitEventHavingMatch<T>
    infix fun having(map: AwaitEventHavingMatches<T, M>.() -> Unit): AwaitEventBut<T>

}

@FlowLanguage
interface AwaitEventHavingMatches<T: Any, M: Any> {

    infix fun String.match (match: T.() -> Any?)

}

@FlowLanguage
interface AwaitEventHavingMatch<T: Any> {

    infix fun match(value: T.() -> Any?): AwaitEventBut<T>

}

@FlowLanguage
interface AwaitEventBut<T: Any> {

    infix fun but(path: Catch<T>.() -> Unit): AwaitEventBut<T>

}
