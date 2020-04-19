package io.factdriven.impl.definition

import io.factdriven.definition.Looping
import io.factdriven.definition.Node
import io.factdriven.language.Loop
import io.factdriven.language.Until
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

class LoopingImpl<T:Any> (entity: KClass<T>, override val parent: Node? = null):

    Loop<T>,

    Looping,
    FlowImpl<T>(entity, parent)

{

    override fun invoke(path: Loop<T>.() -> Unit) {
        apply(path)
    }

    override val until: Until<T>
        get() {
            val child = ConditionalImpl<T>(this)
            children.add(child)
            return child
        }

}