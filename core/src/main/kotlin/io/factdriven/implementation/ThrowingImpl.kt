package io.factdriven.implementation

import io.factdriven.definition.Node
import io.factdriven.definition.Throwing
import io.factdriven.language.Emit
import io.factdriven.language.Issue
import io.factdriven.language.Sentence
import kotlin.reflect.KClass

open class ThrowingImpl<T: Any>(parent: Node):

    Emit<T>,
    Issue<T>,
    Sentence<T>,

    Throwing,
    NodeImpl(parent)

{

    override lateinit var throwing: KClass<*>
    override lateinit var instance: Any.() -> Any

    override fun <M: Any> event(type: KClass<M>): Sentence<T> {
        this.throwing = type
        return this
    }

    override fun <M: Any> command(type: KClass<M>): Sentence<T> {
        this.throwing = type
        return this
    }

    override fun <M : Any> by(instance: T.() -> M) {
        @Suppress("UNCHECKED_CAST")
        this.instance = instance as Any.() -> Any
    }

}
