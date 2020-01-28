package io.factdriven.lang

import io.factdriven.def.Node
import io.factdriven.def.ThrowingImpl
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

class IssueImpl<T: Any>(override val parent: Node): Issue<T>, Sentence<T>, ThrowingImpl(parent) {

    override fun <M: Any> command(type: KClass<M>): Sentence<T> {
        this.throwingType = type
        return this
    }

    override fun <M : Any> by(instance: T.() -> M) {
        @Suppress("UNCHECKED_CAST")
        this.constructor = instance as Any.() -> Any
    }

}
