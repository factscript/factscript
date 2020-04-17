package io.factdriven.language

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface On<T: Any>: OnCommand<T>, Await<T>

@FlowLang
interface OnCommand<T: Any> {

    infix fun <M: Any> command(type: KClass<M>): OnCommandPromise<T>

}

@FlowLang
interface OnCommandPromise<T: Any> {

    infix fun promise(promise: Promise<T>.() -> Unit): Promise<T>

}

@FlowLang
interface Promise<T: Any> {

    val report: PromiseReport<T>

}

@FlowLang
interface PromiseReport<T: Any>: PromiseReportSuccess<T>

@FlowLang
interface PromiseReportSuccess<T: Any> {

    infix fun <M: Any> success(type: KClass<M>)

}
