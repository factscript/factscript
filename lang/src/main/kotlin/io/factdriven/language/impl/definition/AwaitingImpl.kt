package io.factdriven.language.impl.definition

import io.factdriven.execution.Message
import io.factdriven.execution.Receptor
import io.factdriven.execution.Type
import io.factdriven.execution.type
import io.factdriven.language.impl.utils.getValue
import io.factdriven.language.*
import io.factdriven.language.definition.*
import io.factdriven.language.impl.utils.asType
import kotlin.reflect.KClass

open class AwaitingImpl<T: Any>(parent: Node):

    Await<T>,
    AwaitEventHaving<T>,
    AwaitEventHavingMatch<T>,

    Awaiting,
    NodeImpl(parent)

{

    override lateinit var catching: KClass<out Any>
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
    override fun first(path: AwaitingExecution<T>.() -> Unit): AwaitOr<T> {
        val branch = BranchingImpl<T>(parent!!)
        branch.gateway = Gateway.Catching
        (parent as NodeImpl).children.remove(this)
        (parent as NodeImpl).children.add(branch)
        val flow = AwaitingExecutionImpl(
            entity as KClass<T>,
            branch
        ).apply(path)
        (branch as NodeImpl).children.add(flow)
        return branch
    }

    override fun time(cycle: AwaitTimeCycle<T>): AwaitTimeCycleFromLimitTimes<T> {
        TODO("Not yet implemented")
    }

    override fun time(duration: AwaitTimeDuration<T>): AwaitTimeFrom<T, Unit> {
        TODO("Not yet implemented")
    }

    override fun time(limit: AwaitTimeLimit<T>) {
        TODO("Not yet implemented")
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

    override fun isSucceeding(): Boolean {
        return Flows.find(reporting = catching)?.find(Promising::class)?.succeeding == catching
    }

    override fun isFailing(): Boolean {
        return Flows.find(reporting = catching)?.find(Promising::class)?.failing?.contains(catching) == true
    }

}
