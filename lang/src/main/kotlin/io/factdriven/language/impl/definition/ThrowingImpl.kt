package io.factdriven.language.impl.definition

import io.factdriven.language.definition.Node
import io.factdriven.language.definition.Throwing
import io.factdriven.execution.Type
import io.factdriven.execution.type
import io.factdriven.language.Emit
import io.factdriven.language.Issue
import io.factdriven.language.By
import io.factdriven.language.ExecuteBy
import io.factdriven.language.definition.Promising
import kotlin.reflect.KClass

open class ThrowingImpl<T: Any, F: Any>(parent: Node):

    Emit<T>,
    Issue<T>,
    By<T, Any>,

    Throwing,
    NodeImpl(parent)

{

    override lateinit var throwing: KClass<*>
    override lateinit var instance: Any.() -> Any

    override val type: Type get() = throwing.type

    @Suppress("UNCHECKED_CAST")
    override fun <M : Any> event(type: KClass<M>): By<T, M> {
        this.throwing = type
        return this as By<T, M>
    }

    override fun <M : Any> command(type: KClass<M>): By<T, M> {
        return event(type)
    }

    @Suppress("UNCHECKED_CAST")
    override fun by(instance: T.() -> Any): F {
        this.instance = instance as Any.() -> Any
        @Suppress("UNCHECKED_CAST")
        return this as F
    }

    override fun isSucceeding(): Boolean {
        return root.find(nodeOfType = Promising::class)?.succeeding == throwing
    }

    override fun isFailing(): Boolean {
        return root.find(nodeOfType = Promising::class)?.failing?.contains(throwing) ?: false
    }

    override fun isContinuing(): Boolean {
        return !isSucceeding() && !isFailing()
    }

}
