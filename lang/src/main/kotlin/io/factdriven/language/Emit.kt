package io.factdriven.language

import io.factdriven.language.impl.definition.ExecutingImpl
import io.factdriven.language.impl.definition.ThrowingImpl
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Emit<T: Any>: EmitEvent<T>

@FlowLanguage
interface  EmitEvent<T: Any> {

    @Deprecated("Replaced by event(instance: T.() -> M)")
    infix fun <M: Any> event(type: KClass<M>): By<T, M>

}

inline infix fun <T: Any, reified M: Any> EmitEvent<T>.event(noinline instance: T.() -> M) {
    when (this) {
        is ExecutingImpl<T> -> event(M::class).by(instance)
        is ThrowingImpl<T, *> -> event(M::class).by(instance)
    }
}