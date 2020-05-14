package io.factdriven.language

import io.factdriven.language.impl.definition.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface On<T: Any>: OnCommand<T>, Await<T>

@FlowLanguage
interface OnCommand<T: Any>

infix fun <T: Any, M: Any> OnCommand<T>.command(type: KClass<M>): OnCommandEmit<T> {
    return (this as PromisingImpl<T>).command(type)
}

@FlowLanguage
interface OnCommandHaving<T: Any, M: Any>: OnCommandEmit<T>, AwaitEventHaving<T, M> {

    override infix fun having(property: String): OnCommandHavingMatch<T>
    infix fun having(map: OnCommandHavingMatches<T, M>.() -> Unit): OnCommandEmit<T>

}

@FlowLanguage
interface OnCommandHavingMatches<T: Any, M: Any>: AwaitEventHavingMatches<T, M>

@FlowLanguage
interface OnCommandHavingMatch<T: Any>: AwaitEventHavingMatch<T> {

    override infix fun match(value: T.() -> Any?): OnCommandEmit<T>

}

@FlowLanguage
interface OnCommandEmit<T: Any>: AwaitEventBut<T> {

    infix fun emit(emit: OnCommandEmitEvent<T>.() -> Unit): AwaitEventBut<T>

}

@FlowLanguage
interface OnCommandEmitEvent<T: Any>: OnCommandEmitEventSuccess<T>, OnCommandEmitEventFailure<T>

@FlowLanguage
interface OnCommandEmitEventSuccess<T: Any> {

    val success: OnCommandEmitEventType<T>

}

@FlowLanguage
interface OnCommandEmitEventFailure<T: Any> {

    val failure: OnCommandEmitEventType<T>

}

@FlowLanguage
interface OnCommandEmitEventType<T: Any>

infix fun <T: Any, M: Any> OnCommandEmitEventType<T>.event(type: KClass<M>) = (this as PromisingImpl<T>).emit(type)