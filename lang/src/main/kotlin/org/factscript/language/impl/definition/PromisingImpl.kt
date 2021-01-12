package org.factscript.language.impl.definition

import org.factscript.language.*
import org.factscript.language.definition.*
import kotlin.reflect.KClass

open class PromisingImpl<T: Any>(parent: Node):

    On<T>,
    OnCommandHaving<T, Any>,
    OnCommandHavingMatch<T>,
    OnCommandEmit<T>,
    OnCommandEmitEvent<T>,
    OnCommandEmitEventType<T>,

    Promising,
    CorrelatingImpl<T>(parent)

{

    var factQuality: FactQuality = FactQuality.Success
    override var successType: KClass<*>? = null
    override val failureTypes: MutableList<KClass<*>> = mutableListOf()

    fun <M : Any> command(type: KClass<M>): OnCommandHaving<T, M> {
        this.consuming = type
        @Suppress("UNCHECKED_CAST")
        return this as OnCommandHaving<T, M>
    }

    override fun having(property: String): OnCommandHavingMatch<T> {
        return super.having(property) as OnCommandHavingMatch<T>
    }

    override fun having(map: OnCommandHavingMatches<T, Any>.() -> Unit): OnCommandEmit<T> {
        @Suppress("UNCHECKED_CAST")
        super.having(map as AwaitEventHavingMatches<T, Any>.() -> Unit)
        return this
    }

    override fun match(value: T.() -> Any?): OnCommandEmit<T> {
        return super.match(value) as OnCommandEmit<T>
    }

    override fun emit(emit: OnCommandEmitEvent<T>.() -> Unit): AwaitEventBut<T> {
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
