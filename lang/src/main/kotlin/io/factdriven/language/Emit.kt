package io.factdriven.language

import io.factdriven.language.impl.definition.ThrowingImpl
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Emit<T: Any>: EmitEvent<T>

@FlowLanguage
interface  EmitEvent<T: Any>: EmitEventSuccess<T>, EmitEventFailure<T> {

    infix fun <M: Any> event(type: KClass<M>): By<T, M>

}

@FlowLanguage
interface EmitEventSuccess<T: Any> {

    infix fun success(event: EmitEventFactory)

}

@FlowLanguage
interface EmitEventFailure<T: Any> {

    infix fun failure(event: EmitEventFactory)

}

inline infix fun <T: Any, reified M: Any> EmitEvent<T>.event(noinline instance: T.() -> M) {
    (this as ThrowingImpl<T, *>).event(M::class).by(instance)
}

inline fun <T: Any, reified M: Any> Execution<T>.event(noinline instance: T.() -> M): EmitEventFactory {
    @Suppress("UNCHECKED_CAST")
    return EmitEventFactory(M::class, instance as Any.() -> Any)
}

data class EmitEventFactory(val throwing: KClass<*>, val factory: Any.() -> Any)
