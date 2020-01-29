package io.factdriven.lang

import io.factdriven.def.ExecutingImpl
import io.factdriven.def.Node
import io.factdriven.def.ThrowingImpl
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface Execute<T: Any>: IssueCommand<T>

@FlowLang
interface ExecuteCommand<T: Any> {

    infix fun <M: Any> command(type: KClass<M>): Sentence<T>

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
