package io.factdriven.language

import io.factdriven.definition.api.Executing
import io.factdriven.definition.impl.ThrowingImpl
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface Issue<T: Any>: IssueCommand<T>

@FlowLang
interface IssueCommand<T: Any> {

    infix fun <M: Any> command(type: KClass<M>): Sentence<T>

}

class IssueImpl<T: Any>(parent: Executing): Issue<T>, Sentence<T>, ThrowingImpl(parent) {

    override fun <M: Any> command(type: KClass<M>): Sentence<T> {
        this.throwing = type
        return this
    }

    override fun <M : Any> by(instance: T.() -> M) {
        @Suppress("UNCHECKED_CAST")
        this.instance = instance as Any.() -> Any
    }

}
