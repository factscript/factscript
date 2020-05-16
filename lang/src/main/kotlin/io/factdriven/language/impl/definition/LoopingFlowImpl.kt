package io.factdriven.language.impl.definition

import io.factdriven.language.*
import io.factdriven.language.definition.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

class LoopingFlowImpl<T:Any> (entity: KClass<T>, override val parent: Node? = null):

    Loop<T>,
    Repeat<T>,

    RepeatingFlow,
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

    override val condition: (Any.() -> Boolean) get() = (children.last() as Conditional).condition!!

    override fun isFailing(): Boolean {
        return false
    }

    override fun isSucceeding(): Boolean {
        return false
    }

    override val description: String get() = children.last().description

}