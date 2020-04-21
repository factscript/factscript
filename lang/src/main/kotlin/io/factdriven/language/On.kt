package io.factdriven.language

import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLanguage
interface On<T: Any>: OnCommand<T>, Await<T>

@FlowLanguage
interface OnCommand<T: Any> {

    infix fun <M: Any> command(type: KClass<M>): OnCommandPromise<T>

}

@FlowLanguage
interface OnCommandPromise<T: Any> {

    infix fun promise(promise: OnCommandPromiseReportSuccess<T>.() -> Unit): OnCommandPromiseReportSuccess<T>

}

@FlowLanguage
interface OnCommandPromiseReportSuccess<T: Any> {

    val report: Report<T>

}
