package io.factdriven.language.impl.definition

import io.factdriven.language.definition.Node
import io.factdriven.language.*
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
open class PromisingExecutionImpl<T:Any>(entity: KClass<T>, override val parent: Node? = null):

    PromisingExecution<T>,

    AwaitingExecutionImpl<T>(entity, parent)

{

    override val on: On<T>
        get() {
            val child = PromisingImpl<T>(this)
            children.add(child)
            return child
        }

}