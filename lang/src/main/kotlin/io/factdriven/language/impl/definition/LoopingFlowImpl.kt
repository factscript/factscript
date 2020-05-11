package io.factdriven.language.impl.definition

import io.factdriven.language.Loop
import io.factdriven.language.Until
import io.factdriven.language.definition.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

class LoopingFlowImpl<T:Any> (entity: KClass<T>, override val parent: Node? = null):

    Loop<T>,

    LoopingFlow,
    FlowImpl<T>(entity, parent)

{

    override val until: Until<T>
        get() {
            val child = ConditionalImpl<T>(this)
            children.add(child)
            return child
        }

    override val condition: (Any.() -> Boolean) get() = (children.last() as Conditional).condition!!

    override fun isFailing(): Boolean {
        return false
    }

    override fun isSucceeding(): Boolean {
        return false
    }

    override val description: String get() = children.last().description

}