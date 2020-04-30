package io.factdriven.language.impl.definition

import io.factdriven.language.*
import io.factdriven.language.definition.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
open class AwaitingExecutionImpl<T:Any>(entity: KClass<T>, override val parent: Node? = null):

    AwaitingExecution<T>,

    AwaitingFlow,
    ExecutionImpl<T>(entity, parent)

{

    override val on: Await<T>
        get() {
            val child = AwaitingImpl<T>(this)
            children.add(child)
            return child
        }

    override val catching: KClass<*> get() = find(Catching::class)!!.catching
    override val matching: List<Any.() -> Any?> get() = find(Awaiting::class)!!.matching
    override val properties: List<String> get() = find(Awaiting::class)!!.properties

}