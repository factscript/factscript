package io.factdriven.language.impl.definition

import io.factdriven.language.Flows
import io.factdriven.language.definition.Calling
import io.factdriven.language.definition.Gateway
import io.factdriven.language.definition.Node
import io.factdriven.language.definition.Promising
import io.factdriven.execution.Message
import io.factdriven.execution.Receptor
import io.factdriven.language.Execute
import io.factdriven.language.ExecuteAnd
import io.factdriven.language.Execution
import io.factdriven.language.Sentence
import kotlin.reflect.KClass

open class CallingImpl<T: Any>(parent: Node):

    Execute<T>,
    Sentence<T, Any>,
    Calling,
    ThrowingImpl<T>(parent)

{

    override val catching: KClass<*> get() = Flows.get(handling = throwing).find(nodeOfType = Promising::class)!!.succeeding!!

    override fun all(path: Execution<T>.() -> Unit): ExecuteAnd<T> {
        val branch = BranchingImpl<T>(parent!!)
        branch.gateway = Gateway.Parallel
        (parent as NodeImpl).children.remove(this)
        (parent as NodeImpl).children.add(branch)
        @Suppress("UNCHECKED_CAST")
        val flow = TriggeredExecutionImpl(entity as KClass<T>, branch).apply(path)
        (branch as NodeImpl).children.add(flow)
        return branch
    }

    override fun findReceptorsFor(message: Message): List<Receptor> {
        return if (catching.isInstance(message.fact.details) && message.correlating != null)
            listOf(Receptor(catching, message.correlating))
        else emptyList()
    }

}
