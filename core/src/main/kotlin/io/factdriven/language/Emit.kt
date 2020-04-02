package io.factdriven.language

import io.factdriven.definition.api.Executing
import io.factdriven.definition.impl.ThrowingImpl
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface Emit<T: Any>: EmitEvent<T>

@FlowLang
interface EmitEvent<T: Any> {

    infix fun <M: Any> event(type: KClass<M>): Sentence<T>

}

class EmitImpl<T: Any>(parent: Executing): Emit<T>, Sentence<T>, ThrowingImpl(parent) {

    override fun <M: Any> event(type: KClass<M>): Sentence<T> {
        this.throwing = type
        return this
    }

    override fun <M : Any> by(instance: T.() -> M) {
        @Suppress("UNCHECKED_CAST")
        this.instance = instance as Any.() -> Any
    }

}
