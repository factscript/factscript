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

    Executing,
    ThrowingImpl<T, ExecuteBut<T>>(parent)

{

    override val consuming: KClass<*> get() = successType
    override val successType: KClass<*> get() = Flows.find(handling = throwing)!!.filter(Promising::class).find { it.consuming == throwing }!!.successType!!
    override val failureTypes: List<KClass<*>> get() = Flows.all().filterIsInstance<PromisingFlow>().find { flow -> flow.consuming == throwing  }?.failureTypes ?: emptyList()

    override fun command(type: KClass<*>, factory: Any.() -> Any): ExecuteBut<T> {
        this.factType = FactType.Command
        this.throwing = type
        this.factory = factory
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun all(path: Execution<T>.() -> Unit): ExecuteAnd<T> {
        val branch = BranchingImpl<T>(parent!!)
        branch.fork = Junction.All
        (parent as NodeImpl).children.remove(this)
        (parent as NodeImpl).children.add(branch)
        val flow = FlowImpl(entity as KClass<T>, branch).apply(path)
        (branch as NodeImpl).children.add(flow)
        return branch
    }

    @Suppress("UNCHECKED_CAST")
    override fun loop(path: Loop<T>.() -> Unit) {
        val loop = LoopingFlowImpl<T>(entity as KClass<T>, parent!!)
        (parent as NodeImpl).children.remove(this)
        loop.apply(path)
        (parent as NodeImpl).children.add(loop)
    }

    @Suppress("UNCHECKED_CAST")
    override fun but(path: Catch<T>.() -> Unit): ExecuteBut<T> {
        val flow = CorrelatingFlowImpl(
            entity as KClass<T>,
            this
        ).apply(path)
        children.add(flow)
        return this
    }

    override fun findReceptorsFor(message: Message): Set<Receptor> {
        return (if (successType.isInstance(message.fact.details) || failureTypes.contains(message.fact.details::class) && message.correlating != null)
            listOf(Receptor(correlating = message.correlating))
        else emptyList()).toSet()
    }

    override fun isCompensating(): Boolean {
        return (parent as? Flow)?.isCompensating() == true
    }

}
