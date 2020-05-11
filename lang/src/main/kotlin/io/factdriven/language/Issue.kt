package io.factdriven.language

import io.factdriven.language.impl.definition.ThrowingImpl
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Issue<T: Any>: IssueCommand<T>

@FlowLanguage
interface IssueCommand<T: Any> {

    infix fun <M: Any> command(type: KClass<M>): By<T, M>

}

inline infix fun <T: Any, reified M: Any> IssueCommand<T>.command(noinline instance: T.() -> M) {
    (this as ThrowingImpl<T, *>).command(M::class).by(instance)
}
