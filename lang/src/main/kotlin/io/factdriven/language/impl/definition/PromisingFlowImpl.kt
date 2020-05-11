package io.factdriven.language.impl.definition

import io.factdriven.language.definition.Node
import io.factdriven.language.*
import io.factdriven.language.definition.Consuming
import io.factdriven.language.definition.Promising
import io.factdriven.language.definition.PromisingFlow
import kotlin.reflect.KClass

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
open class PromisingFlowImpl<T:Any>(entity: KClass<T>, override val parent: Node? = null):

    Promise<T>,

    PromisingFlow,
    FlowImpl<T>(entity, parent)

{

    override val on: On<T>
        get() {
            val child = PromisingImpl<T>(this)
            children.add(child)
            return child
        }

    override val consuming: KClass<*> get() = find(Consuming::class)!!.consuming
    override val success: KClass<*>? get() = find(Promising::class)!!.success
    override val failure: List<KClass<*>> get() = find(Promising::class)!!.failure

}