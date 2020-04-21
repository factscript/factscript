package io.factdriven.impl.definition

import io.factdriven.definition.Looping
import io.factdriven.definition.Node
import io.factdriven.language.LoopingExecution
import io.factdriven.language.Until
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

class LoopingExecutionImpl<T:Any> (entity: KClass<T>, override val parent: Node? = null):

    LoopingExecution<T>,

    Looping,
    TriggeredExecutionImpl<T>(entity, parent)

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

}