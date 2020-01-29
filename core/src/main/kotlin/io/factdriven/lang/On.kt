package io.factdriven.lang

import io.factdriven.def.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
@FlowLang
interface On<T: Any>: OnCommand<T>

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


class OnImpl<T: Any>(parent: Node): On<T>, Promise<T>, OnCommandPromise<T>, PromiseReport<T>, PromisingImpl(parent) {

    override fun <M : Any> command(type: KClass<M>): OnCommandPromise<T> {
        this.catchingType = type
        return this
    }

    override fun promise(promise: Promise<T>.() -> Unit): Promise<T> {
        this.apply(promise)
        return this
    }

    override val report: PromiseReport<T> = this

    override fun <M : Any> success(type: KClass<M>) {
        this.successType = type
    }

}