package org.factscript.language

import org.factscript.language.impl.definition.ExecutingImpl
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Execute<T: Any>: ExecuteCommand<T>, ExecuteAll<T>

@FlowLanguage
interface ExecuteCommand<T: Any>

inline infix fun <T: Any, reified M: Any> ExecuteCommand<T>.command(noinline instance: T.() -> M): ExecuteBut<T> {
    @Suppress("UNCHECKED_CAST")
    return (this as ExecutingImpl<T>).command(M::class, instance as Any.() -> Any)
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
interface ExecuteBut<T: Any> {

    infix fun but(path: Catch<T>.() -> Unit): ExecuteBut<T>

}
