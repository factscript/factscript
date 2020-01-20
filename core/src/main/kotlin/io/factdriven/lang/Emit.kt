package io.factdriven.lang

import io.factdriven.def.Node
import io.factdriven.def.ThrowingImpl
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

class EmitImpl<T: Any>(override val parent: Node): Emit<T>, Sentence<T>, ThrowingImpl(parent) {

    override fun <M: Any> event(type: KClass<M>): Sentence<T> {
        this.throwingType = type
        return this
    }

    override fun <M : Any> by(instance: T.() -> M) {
        @Suppress("UNCHECKED_CAST")
        this.constructor = instance as Any.() -> Any
    }

}
