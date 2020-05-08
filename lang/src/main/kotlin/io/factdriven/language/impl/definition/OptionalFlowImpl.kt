package io.factdriven.language.impl.definition

import io.factdriven.language.Given
import io.factdriven.language.Option
import io.factdriven.language.definition.Conditional
import io.factdriven.language.definition.Node
import io.factdriven.language.definition.OptionalFlow
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

    override val condition: (Any.() -> Boolean)? get() = find(Conditional::class)?.condition

    override val description: String get() = find(Conditional::class)!!.description

}