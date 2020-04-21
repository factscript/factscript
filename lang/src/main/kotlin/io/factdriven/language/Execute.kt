package io.factdriven.language

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Execute<T: Any>: ExecuteCommand<T>, ExecuteAll<T>

@FlowLanguage
interface ExecuteCommand<T: Any> {

    infix fun <M: Any> command(type: KClass<M>): Sentence<T>

}

@FlowLanguage
interface ExecuteAll<T: Any> {

    infix fun all(path: Execution<T>.() -> Unit): ExecuteAnd<T>

}

@FlowLanguage
interface ExecuteAnd<T: Any> {

    infix fun and(path: Execution<T>.() -> Unit): ExecuteAnd<T>

}
