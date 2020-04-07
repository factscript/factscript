package io.factdriven.language

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface Execute<T: Any>: ExecuteCommand<T>

@FlowLang
interface ExecuteCommand<T: Any> {

    infix fun <M: Any> command(type: KClass<M>): Sentence<T>

}

@FlowLang
interface ExecuteAll<T: Any> {

    infix fun all(path: Execution<T>.() -> Unit): ExecuteAnd<T>

}

@FlowLang
interface ExecuteAnd<T: Any> {

    infix fun and(path: Execution<T>.() -> Unit): ExecuteAnd<T>

}
