package org.factscript.language.impl.definition

import org.factscript.language.definition.Node
import org.factscript.language.*
import org.factscript.language.definition.Consuming
import org.factscript.language.definition.Promising
import org.factscript.language.definition.PromisingFlow
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
    override val successType: KClass<*>? get() = find(Promising::class)!!.successType
    override val failureTypes: List<KClass<*>> get() = find(Promising::class)!!.failureTypes

}