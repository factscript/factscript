package org.factscript.language.impl.definition

import org.factscript.language.Given
import org.factscript.language.Option
import org.factscript.language.Otherwise
import org.factscript.language.definition.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

class OptionalFlowImpl<T:Any> (entity: KClass<T>, override val parent: Node? = null):

    Option<T>,

    OptionalFlow,
    FlowImpl<T>(entity, parent)

{

    override val given: Given<T>
        get() {
            val child = ConditionalImpl<T>(this)
            children.add(child)
            return child
        }


    @Suppress("UNCHECKED_CAST")
    override val otherwise: Otherwise<T, Unit>
        get() {
            val child = ConditionalImpl<T>(this)
            children.add(child)
            return child as Otherwise<T, Unit>
        }

    override val condition: (Any.() -> Boolean)? get() = find(ConditionalNode::class)?.condition

    override val description: String get() = find(ConditionalNode::class)!!.description

}