package io.factdriven.language.impl.definition

import io.factdriven.language.definition.Branching
import io.factdriven.language.definition.Node
import io.factdriven.language.definition.Split
import io.factdriven.execution.Type
import io.factdriven.execution.type
import io.factdriven.language.*
import kotlin.reflect.KClass

open class BranchingImpl<T: Any>(parent: Node):

    Select<T>,
    SelectOr<T>,
    AwaitOr<T>,
    ExecuteAnd<T>,
    Branching,
    NodeImpl(parent)

{

    override lateinit var split: Split
    override var description: String = ""; protected set

    override val type: Type
        get() = Type(
            entity.type.context,
            split.name
        )

    override fun either(path: Option<T>.() -> Unit): SelectOr<T> {
        split = Split.Exclusive
        return or(path)
    }

    override fun all(path: Option<T>.() -> Unit): SelectOr<T> {
        split = Split.Inclusive
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

    override fun isConditional(): Boolean = split == Split.Exclusive || split == Split.Inclusive

}
