package io.factdriven.impl.definition

import io.factdriven.definition.Node
import io.factdriven.definition.Promising
import io.factdriven.language.On
import io.factdriven.language.OnCommandPromise
import io.factdriven.language.OnCommandPromiseReportSuccess
import io.factdriven.language.Report
import kotlin.reflect.KClass

open class PromisingImpl<T: Any>(parent: Node):

    On<T>,
    OnCommandPromiseReportSuccess<T>, OnCommandPromise<T>,
    Report<T>,

    Promising,
    AwaitingImpl<T>(parent)

{

    override var succeeding: KClass<*>? = null

    override fun <M : Any> command(type: KClass<M>): OnCommandPromise<T> {
        this.catching = type
        return this
    }

    override fun promise(promise: OnCommandPromiseReportSuccess<T>.() -> Unit): OnCommandPromiseReportSuccess<T> {
        this.apply(promise)
        return this
    }

    override val report: Report<T> = this

    override fun <M : Any> success(type: KClass<M>) {
        this.succeeding = type
    }

}
