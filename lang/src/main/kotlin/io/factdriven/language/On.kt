package io.factdriven.language

import io.factdriven.language.impl.definition.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface On<T: Any>: OnCommand<T>, Await<T>

@FlowLanguage
interface OnCommand<T: Any>

infix fun <T: Any, M: Any> OnCommand<T>.command(type: KClass<M>): OnCommandPromise<T> {
    return (this as PromisingImpl<T>).command(type)
}

@FlowLanguage
interface OnCommandPromise<T: Any> {

    infix fun promise(promise: OnCommandPromiseReport<T>.() -> Unit): OnCommandPromiseReport<T>

}

@FlowLanguage
interface OnCommandPromiseReport<T: Any> {

    val report: Report<T>

}
