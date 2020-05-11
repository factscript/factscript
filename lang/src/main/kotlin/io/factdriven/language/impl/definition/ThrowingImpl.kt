package io.factdriven.language.impl.definition

import io.factdriven.execution.Type
import io.factdriven.execution.type
import io.factdriven.language.*
import io.factdriven.language.definition.*
import kotlin.reflect.KClass

open class ThrowingImpl<T: Any, F: Any>(parent: Node):

    Emit<T>,
    Issue<T>,
    By<T, Any>,

    Throwing,
    NodeImpl(parent)

{

    override lateinit var throwing: KClass<*>
    override lateinit var factory: Any.() -> Any
    override lateinit var factType: FactType
    override var factQuality: FactQuality? = null

    override val type: Type get() = throwing.type

    @Suppress("UNCHECKED_CAST")
    override fun <M : Any> event(type: KClass<M>): By<T, M> {
        this.factType = FactType.Event
        this.throwing = type
        return this as By<T, M>
    }

    @Suppress("UNCHECKED_CAST")
    override fun <M : Any> command(type: KClass<M>): By<T, M> {
        this.factType = FactType.Command
        this.throwing = type
        return this as By<T, M>
    }

    @Suppress("UNCHECKED_CAST")
    override fun by(instance: T.() -> Any): F {
        this.factory = instance as Any.() -> Any
        @Suppress("UNCHECKED_CAST")
        return this as F
    }

    override fun isSucceeding(): Boolean {
        return root.find(nodeOfType = Promising::class)?.successType == throwing
    }

    override fun isFailing(): Boolean {
        return root.find(nodeOfType = Promising::class)?.failureTypes?.contains(throwing) == true
    }

    override fun success(event: EmitEventFactory) {
        factQuality = FactQuality.Success
        @Suppress("UNCHECKED_CAST")
        (event(event.throwing) as By<T, Any>).by(event.factory)
    }

    override fun failure(event: EmitEventFactory) {
        factQuality = FactQuality.Failure
        @Suppress("UNCHECKED_CAST")
        (event(event.throwing) as By<T, Any>).by(event.factory)
    }

}
