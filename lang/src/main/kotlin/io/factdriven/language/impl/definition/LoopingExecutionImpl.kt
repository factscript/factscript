package io.factdriven.language.impl.definition

import io.factdriven.language.LoopingExecution
import io.factdriven.language.Until
import io.factdriven.language.definition.Looping
import io.factdriven.language.definition.Node
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

class LoopingExecutionImpl<T:Any> (entity: KClass<T>, override val parent: Node? = null):

    LoopingExecution<T>,

    Looping,
    ExecutionImpl<T>(entity, parent)

{

    override fun invoke(path: LoopingExecution<T>.() -> Unit) {
        apply(path)
    }

    override val until: Until<T>
        get() {
            val child = ConditionalImpl<T>(this)
            children.add(child)
            return child
        }

    override fun isFailing(): Boolean {
        return false
    }

    override fun isSucceeding(): Boolean {
        return false
    }

}