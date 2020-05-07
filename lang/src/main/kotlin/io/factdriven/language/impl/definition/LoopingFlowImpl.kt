package io.factdriven.language.impl.definition

import io.factdriven.language.Loop
import io.factdriven.language.Until
import io.factdriven.language.definition.Conditional
import io.factdriven.language.definition.LoopingFlow
import io.factdriven.language.definition.Node
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

class LoopingFlowImpl<T:Any> (entity: KClass<T>, override val parent: Node? = null):

    Loop<T>,

    LoopingFlow,
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

    override val condition: (Any.() -> Boolean) get() = find(Conditional::class)!!.condition!!

    override fun isFailing(): Boolean {
        return false
    }

    override fun isSucceeding(): Boolean {
        return false
    }

}