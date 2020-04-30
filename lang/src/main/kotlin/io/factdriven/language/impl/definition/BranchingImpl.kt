package io.factdriven.language.impl.definition

import io.factdriven.language.definition.Branching
import io.factdriven.language.definition.Node
import io.factdriven.language.definition.Gateway
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

    override lateinit var gateway: Gateway
    override var label: String = ""; protected set

    override val type: Type
        get() = Type(
            entity.type.context,
            gateway.name
        )

    override fun either(path: ConditionalExecution<T>.() -> Unit): SelectOr<T> {
        gateway = Gateway.Exclusive
        return or(path)
    }

    override fun all(path: ConditionalExecution<T>.() -> Unit): SelectOr<T> {
        gateway = Gateway.Inclusive
        return or(path)
    }

    @Suppress("UNCHECKED_CAST")
    override fun or(path: ConditionalExecution<T>.() -> Unit): SelectOr<T> {
        val flow = ConditionalExecutionImpl(
            entity as KClass<T>,
            this
        ).apply(path)
        children.add(flow)
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun or(path: TriggeredExecution<T>.() -> Unit): AwaitOr<T> {
        val flow = TriggeringExecutionImpl(
            entity as KClass<T>,
            this
        ).apply(path)
        children.add(flow)
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun and(path: Execution<T>.() -> Unit): ExecuteAnd<T> {
        @Suppress("UNCHECKED_CAST")
        val flow = TriggeringExecutionImpl(
            entity as KClass<T>,
            this
        ).apply(path)
        children.add(flow)
        return this
    }

    override fun invoke(case: String): Select<T> {
        this.label = case
        return this
    }

    override fun isConditional(): Boolean = gateway == Gateway.Exclusive || gateway == Gateway.Inclusive

}
