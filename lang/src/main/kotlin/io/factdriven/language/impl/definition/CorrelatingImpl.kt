package io.factdriven.language.impl.definition

import io.factdriven.execution.Message
import io.factdriven.execution.Receptor
import io.factdriven.execution.Type
import io.factdriven.execution.type
import io.factdriven.language.*
import io.factdriven.language.definition.Consuming
import io.factdriven.language.definition.Correlating
import io.factdriven.language.definition.Split
import io.factdriven.language.definition.Node
import io.factdriven.language.impl.utils.getValue
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class CorrelatingImpl<T: Any>(parent: Node):

    Await<T>,
    AwaitEventHaving<T, Any>,
    AwaitEventHavingMatch<T>,

    Correlating,
    NodeImpl(parent)

{

    override lateinit var consuming: KClass<out Any>
    override val properties = mutableListOf<String>()
    override val matching = mutableListOf<Any.() -> Any?>()

    override val type: Type get() = consuming.type

    override fun <M : Any> event(type: KClass<M>): AwaitEventHaving<T, M> {
        this.consuming = type
        @Suppress("UNCHECKED_CAST")
        return this as AwaitEventHaving<T, M>
    }

    override fun having(property: String): AwaitEventHavingMatch<T> {
        this.properties.add(property)
        return this
    }

    override fun having(property: KProperty1<Any, *>): AwaitEventHavingMatch<T> {
        return having(property.name)
    }

    override fun having(map: AwaitEventHavingMatches<T, Any>.() -> Unit): AwaitEventBut<T> {
        val matches = AwaitEventHavingMatchesImpl<T>()
        matches.apply(map)
        properties.addAll(matches.properties)
        @Suppress("UNCHECKED_CAST")
        matching.addAll(matches.matching as Collection<Any.() -> Any?>)
        return this
    }

    override fun match(value: T.() -> Any?): AwaitEventBut<T> {
        @Suppress("UNCHECKED_CAST")
        this.matching.add(value as (Any.() -> Any?))
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun but(path: Catch<T>.() -> Unit): AwaitEventBut<T> {
        val flow = ConsumingFlowImpl(
            entity as KClass<T>,
            this
        )
        flow.apply(path)
        children.add(flow)
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun first(path: Catch<T>.() -> Unit): AwaitOr<T> {
        val branch = BranchingImpl<T>(parent!!)
        branch.split = Split.Waiting
        (parent as NodeImpl).children.remove(this)
        (parent as NodeImpl).children.add(branch)
        val flow = ConsumingFlowImpl(
            entity as KClass<T>,
            branch
        ).apply(path)
        (branch as NodeImpl).children.add(flow)
        return branch
    }

    @Suppress("UNCHECKED_CAST")
    override fun time(duration: AwaitTimeDuration<T>) {
        (parent as NodeImpl).children.remove(this)
        (parent as NodeImpl).children.add(duration as WaitingImpl)
        duration.parent = parent
    }

    override fun time(limit: AwaitTimeLimit<T>) {
        (parent as NodeImpl).children.remove(this)
        (parent as NodeImpl).children.add(limit as WaitingImpl)
        limit.parent = parent
    }

    override fun findReceptorsFor(message: Message): List<Receptor> {
        return if (consuming.isInstance(message.fact.details))
            listOf(
                Receptor(
                    consuming,
                    properties.map { it to message.fact.details.getValue(it) }.toMap()
                )
            )
        else emptyList()
    }

}

class AwaitEventHavingMatchesImpl<T: Any>: AwaitEventHavingMatches<T, Any> {

    val properties: MutableList<String> = mutableListOf()
    val matching: MutableList<T.() -> Any?> = mutableListOf()

    override fun String.match(match: T.() -> Any?) {
        properties.add(this)
        matching.add(match)
    }

    override fun KProperty1<Any, *>.match(match: T.() -> Any?) {
        name.match(match)
    }

}