package io.factdriven.language.impl.definition

import io.factdriven.language.definition.Node
import io.factdriven.language.ConditionalExecution
import io.factdriven.language.Given
import io.factdriven.language.definition.Conditional
import io.factdriven.language.definition.ConditionalFlow
import io.factdriven.language.impl.utils.asType
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

class ConditionalExecutionImpl<T:Any> (entity: KClass<T>, override val parent: Node? = null):

    ConditionalExecution<T>,

    ConditionalFlow,
    TriggeredExecutionImpl<T>(entity, parent)

{

    override val given: Given<T>
        get() {
            val child = ConditionalImpl<T>(this)
            children.add(child)
            return child
        }

    override val condition: (Any.() -> Boolean)? get() = find(Conditional::class)?.condition
    override fun isDefault(): Boolean = condition == null

}