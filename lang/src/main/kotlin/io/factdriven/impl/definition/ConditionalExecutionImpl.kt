package io.factdriven.impl.definition

import io.factdriven.definition.Node
import io.factdriven.language.ConditionalExecution
import io.factdriven.language.Given
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */

class ConditionalExecutionImpl<T:Any> (entity: KClass<T>, override val parent: Node? = null):

    ConditionalExecution<T>,

    TriggeredExecutionImpl<T>(entity, parent)

{

    override val given: Given<T>
        get() {
            val child = ConditionalImpl<T>(this)
            children.add(child)
            return child
        }

}