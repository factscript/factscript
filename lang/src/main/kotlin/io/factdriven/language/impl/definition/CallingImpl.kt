package io.factdriven.language.impl.definition

import io.factdriven.language.definition.Calling
import io.factdriven.language.definition.Gateway
import io.factdriven.language.definition.Node
import io.factdriven.language.definition.Promising
import io.factdriven.execution.Message
import io.factdriven.execution.Receptor
import io.factdriven.language.*
import kotlin.reflect.KClass

open class CallingImpl<T: Any>(parent: Node):

    Execute<T>,
    ExecuteBut<T>,
    ExecuteBy<T, Any>,
    Calling,
    ThrowingImpl<T, ExecuteBut<T>>(parent)

{

    override val catching: KClass<*> get() = succeeding
    override val succeeding: KClass<*> get() = Flows.find(handling = throwing)!!.find(Promising::class)!!.succeeding!!
    override val failing: List<KClass<*>> get() = Flows.find(handling = throwing)!!.find(Promising::class)!!.failing

    @Suppress("UNCHECKED_CAST")
    override fun all(path: Execution<T>.() -> Unit): ExecuteAnd<T> {
        val branch = BranchingImpl<T>(parent!!)
        branch.gateway = Gateway.Parallel
        (parent as NodeImpl).children.remove(this)
        (parent as NodeImpl).children.add(branch)
        val flow = ExecutionImpl(entity as KClass<T>, branch).apply(path)
        (branch as NodeImpl).children.add(flow)
        return branch
    }

    @Suppress("UNCHECKED_CAST")
    override fun <M : Any> command(type: KClass<M>): ExecuteBy<T, M> {
        return super.command(type) as ExecuteBy<T, M>
    }

    @Suppress("UNCHECKED_CAST")
    override fun but(path: AwaitingExecution<T>.() -> Unit): ExecuteBut<T> {
        val flow = AwaitingExecutionImpl(
            entity as KClass<T>,
            this
        ).apply(path)
        children.add(flow)
        return this
    }

    override fun findReceptorsFor(message: Message): List<Receptor> {
        return if (catching.isInstance(message.fact.details) && message.correlating != null)
            listOf(Receptor(catching, message.correlating))
        else emptyList()
    }

}
