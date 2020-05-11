package io.factdriven.language

import io.factdriven.language.impl.definition.ExecutingImpl
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Execute<T: Any>: ExecuteCommand<T>, ExecuteAll<T>

@FlowLanguage
interface ExecuteCommand<T: Any> {

    infix fun <M: Any> command(type: KClass<M>): ExecuteBy<T, M>

}

inline infix fun <T: Any, reified M: Any> ExecuteCommand<T>.command(noinline instance: T.() -> M): ExecuteBut<T> {
    return (this as ExecutingImpl<T>).command(M::class).by(instance)
}

@FlowLanguage
interface ExecuteAll<T: Any> {

    infix fun all(path: Execution<T>.() -> Unit): ExecuteAnd<T>

}

@FlowLanguage
interface ExecuteAnd<T: Any> {

    infix fun and(path: Execution<T>.() -> Unit): ExecuteAnd<T>

}

@FlowLanguage
interface ExecuteBy<T: Any, M: Any>: By<T, M> {

    override fun by(instance: T.() -> M): ExecuteBut<T>

}

@FlowLanguage
interface ExecuteBut<T: Any> {

    infix fun but(path: Catch<T>.() -> Unit): ExecuteBut<T>

}
