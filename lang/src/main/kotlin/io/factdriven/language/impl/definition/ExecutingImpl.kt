package io.factdriven.language.impl.definition

import io.factdriven.execution.Message
import io.factdriven.execution.Receptor
import io.factdriven.language.*
import io.factdriven.language.definition.*
import io.factdriven.language.impl.utils.asType
import kotlin.reflect.KClass

open class ExecutingImpl<T: Any>(parent: Node):

    Execute<T>,
    ExecuteBut<T>,
    ExecuteBy<T, Any>,

    Executing,
    ThrowingImpl<T, ExecuteBut<T>>(parent)

{

    override val consuming: KClass<*> get() = succeeding
    override val succeeding: KClass<*> get() = Flows.find(handling = throwing)!!.asType<PromisingFlow>()!!.succeeding!!
    override val failing: List<KClass<*>> get() = Flows.find(handling = throwing)!!.asType<PromisingFlow>()!!.failing

    @Suppress("UNCHECKED_CAST")
    override fun all(path: Execution<T>.() -> Unit): ExecuteAnd<T> {
        val branch = BranchingImpl<T>(parent!!)
        branch.split = Split.Parallel
        (parent as NodeImpl).children.remove(this)
        (parent as NodeImpl).children.add(branch)
        val flow = FlowImpl(entity as KClass<T>, branch).apply(path)
        (branch as NodeImpl).children.add(flow)
        return branch
    }

    @Suppress("UNCHECKED_CAST")
    override fun <M : Any> command(type: KClass<M>): ExecuteBy<T, M> {
        return super.command(type) as ExecuteBy<T, M>
    }

    @Suppress("UNCHECKED_CAST")
    override fun but(path: Catch<T>.() -> Unit): ExecuteBut<T> {
        val flow = ConsumingFlowImpl(
            entity as KClass<T>,
            this
        ).apply(path)
        children.add(flow)
        return this
    }

    override fun findReceptorsFor(message: Message): List<Receptor> {
        return if (succeeding.isInstance(message.fact.details) || failing.contains(message.fact.details::class) && message.correlating != null)
            listOf(Receptor(correlating = message.correlating))
        else emptyList()
    }

}
