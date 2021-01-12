package org.factscript.language

import org.factscript.language.impl.definition.ThrowingImpl

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface Issue<T: Any>: IssueCommand<T>

@FlowLanguage
interface IssueCommand<T: Any>

inline infix fun <T: Any, reified M: Any> IssueCommand<T>.command(noinline factory: T.() -> M) {
    @Suppress("UNCHECKED_CAST")
    (this as ThrowingImpl<T, *>).command(M::class, factory as Any.() -> Any)
}
