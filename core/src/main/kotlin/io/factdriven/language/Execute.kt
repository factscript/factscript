package io.factdriven.language

import io.factdriven.definition.ExecutingImpl
import io.factdriven.definition.Node
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

class ExecuteImpl<T: Any>(parent: Node): Execute<T>, Sentence<T>, ExecutingImpl(parent) {

    override fun <M: Any> command(type: KClass<M>): Sentence<T> {
        this.throwingType = type
        return this
    }

    override fun <M : Any> by(instance: T.() -> M) {
        @Suppress("UNCHECKED_CAST")
        this.constructor = instance as Any.() -> Any
    }

}
