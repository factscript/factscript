package org.factscript.language.impl.definition

import org.factscript.execution.Type
import org.factscript.execution.type
import org.factscript.language.*
import org.factscript.language.definition.*
import org.factscript.language.impl.utils.*
import kotlin.apply
import kotlin.reflect.KClass

open class BranchingImpl<T: Any>(parent: Node):

    Select<T>,
    SelectOr<T>,
    AwaitOr<T>,
    ExecuteAnd<T>,

    Branching,
    NodeImpl(parent)

{

    val flows: List<Flow> = super.children as List<Flow>

    override lateinit var fork: Junction

    override val join get() = if (flows.count { it.isContinuing() } > 1) when (fork) {
        Junction.First -> Junction.One
        Junction.All -> if (descendants.any { (it as? Flow)?.isSucceeding() == true }) Junction.Some else Junction.All
        else -> fork
    } else null

    override var description: String = ""; protected set

    override fun isFinishing(): Boolean = flows.all { it.isFinishing() }
    override fun isSucceeding(): Boolean = flows.any { it.isSucceeding() }
    override fun isFailing(): Boolean = flows.any { it.isFailing() }

    override val type: Type
        get() = Type(
            entity.type.context,
            fork.name
        )

    override fun either(path: Option<T>.() -> Unit): SelectOr<T> {
        fork = Junction.One
        return or(path)
    }

    override fun all(path: Option<T>.() -> Unit): SelectOr<T> {
        fork = Junction.Some
        return or(path)
    }

    @Suppress("UNCHECKED_CAST")
    override fun or(path: Option<T>.() -> Unit): SelectOr<T> {
        val flow = OptionalFlowImpl(
            entity as KClass<T>,
            this
        ).apply(path)
        children.add(flow)
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun or(path: Catch<T>.() -> Unit): AwaitOr<T> {
        val flow = CorrelatingFlowImpl(
            entity as KClass<T>,
            this
        ).apply(path)
        children.add(flow)
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun and(path: Execution<T>.() -> Unit): ExecuteAnd<T> {
        @Suppress("UNCHECKED_CAST")
        val flow = FlowImpl(
            entity as KClass<T>,
            this
        ).apply(path)
        children.add(flow)
        return this
    }

    override fun invoke(case: String): Select<T> {
        this.description = case
        return this
    }

}
