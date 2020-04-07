package io.factdriven.implementation

import io.factdriven.definition.Node
import io.factdriven.definition.Promising
import io.factdriven.language.On
import io.factdriven.language.OnCommandPromise
import io.factdriven.language.Promise
import io.factdriven.language.PromiseReport
import kotlin.reflect.KClass

open class PromisingImpl<T: Any>(parent: Node):

    On<T>,
    Promise<T>, OnCommandPromise<T>,
    PromiseReport<T>,

    Promising,
    ConsumingImpl<T>(parent)

{

    override var succeeding: KClass<*>? = null

    override fun <M : Any> command(type: KClass<M>): OnCommandPromise<T> {
        this.catching = type
        return this
    }

    override fun promise(promise: Promise<T>.() -> Unit): Promise<T> {
        this.apply(promise)
        return this
    }

    override val report: PromiseReport<T> = this

    override fun <M : Any> success(type: KClass<M>) {
        this.succeeding = type
    }

}
