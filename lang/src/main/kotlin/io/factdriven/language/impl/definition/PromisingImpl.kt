package io.factdriven.language.impl.definition

import io.factdriven.language.definition.Node
import io.factdriven.language.definition.Promising
import io.factdriven.language.On
import io.factdriven.language.OnCommandPromise
import io.factdriven.language.OnCommandPromiseReport
import io.factdriven.language.Report
import kotlin.reflect.KClass

open class PromisingImpl<T: Any>(parent: Node):

    On<T>,
    OnCommandPromiseReport<T>, OnCommandPromise<T>,
    Report<T>,

    Promising,
    CorrelatingImpl<T>(parent)

{

    override var succeeding: KClass<*>? = null
    override val failing: MutableList<KClass<*>> = mutableListOf()

    override fun <M : Any> command(type: KClass<M>): OnCommandPromise<T> {
        this.consuming = type
        return this
    }

    override fun promise(promise: OnCommandPromiseReport<T>.() -> Unit): OnCommandPromiseReport<T> {
        this.apply(promise)
        return this
    }

    override val report: Report<T> = this

    override fun <M : Any> success(type: KClass<M>) {
        this.succeeding = type
    }

    override fun <M : Any> failure(type: KClass<M>) {
        this.failing.add(type)
    }

}
