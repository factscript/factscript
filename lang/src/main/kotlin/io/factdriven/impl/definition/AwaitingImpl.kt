package io.factdriven.impl.definition

import io.factdriven.definition.Awaiting
import io.factdriven.definition.Gateway
import io.factdriven.definition.Node
import io.factdriven.execution.Receptor
import io.factdriven.execution.Message
import io.factdriven.execution.Type
import io.factdriven.execution.type
import io.factdriven.impl.utils.getValue
import io.factdriven.language.*
import kotlin.reflect.KClass

open class AwaitingImpl<T: Any>(parent: Node):

    Await<T>,
    AwaitEventHaving<T>,
    AwaitEventHavingMatch<T>,

    Awaiting,
    NodeImpl(parent)

{

    override lateinit var catching: KClass<*>
    override val properties = mutableListOf<String>()
    override val matching = mutableListOf<Any.() -> Any?>()

    override val type: Type get() = catching.type

    override fun <M : Any> event(type: KClass<M>): AwaitEventHaving<T> {
        this.catching = type
        return this
    }

    override fun having(property: String): AwaitEventHavingMatch<T> {
        this.properties.add(property)
        return this
    }

    override fun match(value: T.() -> Any?) {
        @Suppress("UNCHECKED_CAST")
        this.matching.add(value as (Any.() -> Any?))
    }

    @Suppress("UNCHECKED_CAST")
    override fun first(path: Flow<T>.() -> Unit): AwaitOr<T> {
        val branch = BranchingImpl<T>(parent!!)
        branch.gateway = Gateway.Await
        (parent as NodeImpl).children.remove(this)
        (parent as NodeImpl).children.add(branch)
        val flow = FlowImpl(
            entity as KClass<T>,
            branch
        ).apply(path)
        (branch as NodeImpl).children.add(flow)
        return branch
    }

    override fun findReceptorsFor(message: Message): List<Receptor> {
        return if (catching.isInstance(message.fact.details))
            listOf(
                Receptor(
                    catching,
                    properties.map { it to message.fact.details.getValue(it) }.toMap()
                )
            )
        else emptyList()
    }

}
