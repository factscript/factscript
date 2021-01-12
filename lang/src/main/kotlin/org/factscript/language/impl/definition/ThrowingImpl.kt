package org.factscript.language.impl.definition

import org.factscript.execution.Type
import org.factscript.execution.type
import org.factscript.language.*
import org.factscript.language.definition.*
import kotlin.reflect.KClass

open class ThrowingImpl<T: Any, F: Any>(parent: Node):

    Emit<T>,
    Issue<T>,

    Throwing,
    NodeImpl(parent)

{

    override lateinit var throwing: KClass<*>
    override lateinit var factory: Any.() -> Any
    override lateinit var factType: FactType
    override var factQuality: FactQuality? = null

    override val type: Type get() = throwing.type

    @Suppress("UNCHECKED_CAST")
    fun <M : Any> event(type: KClass<M>, factory: Any.() -> Any) {
        this.factType = FactType.Event
        this.throwing = type
        this.factory = factory
    }

    override fun isSucceeding(): Boolean {
        return root.find(nodeOfType = Promising::class)?.successType == throwing
    }

    override fun isFailing(): Boolean {
        return root.find(nodeOfType = Promising::class)?.failureTypes?.contains(throwing) == true
    }

    override fun isCompensating(): Boolean {
        return isFailing() && root.descendants.filterIsInstance<Executing>().any { it.isCompensating() }
    }

    open fun command(type: KClass<*>, factory: Any.() -> Any): Any {
        this.factType = FactType.Command
        this.throwing = type
        this.factory = factory
        return this
    }

    override fun success(event: EmitEventFactory) {
        this.throwing = event.throwing
        this.factory = event.factory
        this.factType = FactType.Event
        this.factQuality = FactQuality.Success
    }

    override fun failure(event: EmitEventFactory) {
        success(event)
        factQuality = FactQuality.Failure
    }

}
