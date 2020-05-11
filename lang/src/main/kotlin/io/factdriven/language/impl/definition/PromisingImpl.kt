package io.factdriven.language.impl.definition

import io.factdriven.language.On
import io.factdriven.language.OnCommandEmit
import io.factdriven.language.OnCommandEmitEvent
import io.factdriven.language.OnCommandEmitEventType
import io.factdriven.language.definition.*
import kotlin.reflect.KClass

open class PromisingImpl<T: Any>(parent: Node):

    On<T>,
    OnCommandEmitEvent<T>, OnCommandEmit<T>,
    OnCommandEmitEventType<T>,

    Promising,
    CorrelatingImpl<T>(parent)

{

    var factQuality: FactQuality = FactQuality.Success
    override var successType: KClass<*>? = null
    override val failureTypes: MutableList<KClass<*>> = mutableListOf()

    fun <M : Any> command(type: KClass<M>): OnCommandEmit<T> {
        this.consuming = type
        return this
    }

    override fun emit(emit: OnCommandEmitEvent<T>.() -> Unit): OnCommandEmitEvent<T> {
        this.apply(emit)
        return this
    }

    override val success: OnCommandEmitEventType<T> get() {
        factQuality = FactQuality.Success
        return this
    }

    override val failure: OnCommandEmitEventType<T> get() {
        factQuality = FactQuality.Failure
        return this
    }

    fun emit(type: KClass<*>) {
        when(factQuality) {
            FactQuality.Success -> successType = type
            FactQuality.Failure -> failureTypes.add(type)
        }
    }

}
