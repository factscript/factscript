package io.factdriven.implementation

import io.factdriven.definition.Consuming
import io.factdriven.definition.Node
import io.factdriven.language.*
import kotlin.reflect.KClass

open class ConsumingImpl<T: Any>(parent: Node):

    Consume<T>,
    ConsumeEventHaving<T>,
    ConsumeEventHavingMatch<T>,

    Consuming,
    NodeImpl(parent)

{

    override lateinit var catching: KClass<*>
    override val properties = mutableListOf<String>()
    override val matching = mutableListOf<Any.() -> Any?>()

    override fun <M : Any> event(type: KClass<M>): ConsumeEventHaving<T> {
        this.catching = type
        return this
    }

    override fun having(property: String): ConsumeEventHavingMatch<T> {
        this.properties.add(property)
        return this
    }

    override fun match(value: T.() -> Any?) {
        @Suppress("UNCHECKED_CAST")
        this.matching.add(value as (Any.() -> Any?))
    }

    override fun first(path: Flow<T>.() -> Unit): ConsumeOr<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
